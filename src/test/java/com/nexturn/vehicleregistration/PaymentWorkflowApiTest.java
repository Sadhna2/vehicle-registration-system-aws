package com.nexturn.vehicleregistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies stage 3 through real API Gateway proxy events, JPA and local MySQL.
 * No Mockito or real payment gateway is used. Fixtures are uniquely identified
 * and deleted in foreign-key order; the test refuses to connect to RDS.
 */
public class PaymentWorkflowApiTest {
    private static final ObjectMapper JSON = LambdaContainerHandler.getObjectMapper();
    private static final String PASSWORD = "Stage3Test123!";
    private static StreamLambdaHandler handler;
    private String prefix;
    private String employeeEmail;
    private Long ownerId;
    private Long employeeId;
    private Long feeId;
    private String reference;

    @BeforeAll
    static void startApplication() {
        String url = System.getenv("DB_URL");
        assertNotNull(url);
        String address = url.split("\\?")[0];
        assertTrue(address.startsWith("jdbc:mysql://localhost:")
                || address.startsWith("jdbc:mysql://127.0.0.1:"));
        assertTrue(address.endsWith("/nexturn26_aws_test"), "Never run integration tests against RDS.");
        handler = new StreamLambdaHandler();
    }

    @BeforeEach
    void createFixtures() throws Exception {
        prefix = "stage3-" + UUID.randomUUID().toString().replace("-", "");
        employeeEmail = prefix + "-staff@example.com";
        Map<String, Object> owner = new LinkedHashMap<>();
        owner.put("firstName", "WorkflowTest");
        owner.put("lastName", "Owner");
        owner.put("emailAddress", prefix + "@example.com");
        owner.put("phoneNumber", "9876543210");
        owner.put("password", PASSWORD);
        owner.put("dateOfBirth", "1997-01-10");
        owner.put("identityProofType", "PAN");
        owner.put("identityProofNumber", prefix.substring(0, 28));
        owner.put("address", "10 Test Road");
        owner.put("cityName", "Pune");
        owner.put("stateName", "Maharashtra");
        owner.put("pincode", "411001");
        ownerId = call("POST", "/api/auth/signup", owner, 200).path("id").asLong();
        employeeId = call("POST", "/api/employees", employee(), 200).path("employeeId").asLong();
        try (Connection connection = database(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO registration_fee_rules (vehicle_category,application_type,fee_amount,effective_from) "
                        + "VALUES ('PRIVATE_CAR','NEW',1234.56,?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setDate(1, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                feeId = keys.getLong(1);
            }
        }
        reference = call("POST", "/api/applications?ownerId=" + ownerId, vehicle(), 200)
                .path("applicationRefNo").asText();
    }

    /** Deletes only rows linked to this test's generated account IDs and email. */
    @AfterEach
    void removeFixtures() throws Exception {
        if (ownerId == null) {
            return;
        }
        try (Connection connection = database()) {
            String applications = "(SELECT application_ref_no FROM vehicle_registration_applications WHERE applicant_id=?)";
            execute(connection, "DELETE FROM audit_logs WHERE application_ref_no IN " + applications, ownerId);
            execute(connection, "DELETE FROM registration_certificate WHERE temporaryregistration_no IN " + applications, ownerId);
            execute(connection, "DELETE FROM owner_payment_detail WHERE application_id IN " + applications, ownerId);
            execute(connection, "DELETE FROM vehicle_registration_applications WHERE applicant_id=?", ownerId);
            execute(connection, "DELETE FROM vehicle_details WHERE current_owner_id=?", ownerId);
            execute(connection, "DELETE FROM audit_logs WHERE remarks=?", employeeEmail);
            execute(connection, "DELETE FROM audit_logs WHERE remarks=?", "owners:" + ownerId);
            execute(connection, "DELETE FROM audit_logs WHERE remarks=?", "employees:" + employeeId);
            execute(connection, "DELETE FROM owner_details WHERE owner_id=?", ownerId);
            if (employeeId != null) {
                execute(connection, "DELETE FROM rta_employees WHERE employee_id=?", employeeId);
            }
            if (feeId != null) {
                execute(connection, "DELETE FROM registration_fee_rules WHERE fee_rule_id=?", feeId);
            }
        }
    }

    private static Connection database() throws Exception {
        return DriverManager.getConnection(System.getenv("DB_URL"),
                System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    private static void execute(Connection connection, String sql, Object value) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    private Map<String, Object> employee() {
        return Map.of("firstName", "WorkflowTest", "lastName", "Officer", "emailAddress", employeeEmail,
                "phoneNumber", "9876543211", "password", PASSWORD, "designation", "OFFICER", "role", "RTO_OFFICER");
    }

    private Map<String, Object> vehicle() {
        return Map.of("vehicleCategory", "PRIVATE_CAR", "manufacturerName", "Test Manufacturer",
                "modelName", "Test Model", "chassisNumber", "CH-" + prefix, "engineNumber", "EN-" + prefix,
                "fuelType", "PETROL", "manufactureYear", Year.now().getValue(), "colorVariant", "Blue");
    }

    private JsonNode call(String method, String path, Object body, int expected) throws Exception {
        String[] parts = path.split("\\?", 2);
        AwsProxyRequestBuilder request = new AwsProxyRequestBuilder(parts[0], method)
                .header("Accept", "application/json");
        if (parts.length == 2) {
            for (String parameter : parts[1].split("&")) {
                String[] pair = parameter.split("=", 2);
                request.queryString(pair[0], pair[1]);
            }
        }
        if (body == null) {
            request.nullBody();
        } else {
            request.header("Content-Type", "application/json").body(JSON.writeValueAsString(body));
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(request.buildStream(), output, new MockLambdaContext());
        AwsProxyResponse response = JSON.readValue(output.toByteArray(), AwsProxyResponse.class);
        assertEquals(expected, response.getStatusCode(), response.getBody());
        return response.getBody() == null || response.getBody().isBlank()
                ? JSON.nullNode() : JSON.readTree(response.getBody());
    }

    private JsonNode review(String action, String remarks, LocalDate appointment, int status) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", action);
        request.put("remarks", remarks);
        request.put("appointment", appointment == null ? null : appointment.toString());
        return call("POST", "/api/applications/" + reference + "/review?employeeId=" + employeeId, request, status);
    }

    private JsonNode pay() throws Exception {
        return call("POST", "/api/applications/" + reference + "/payments",
                Map.of("amount", new BigDecimal("1234.56"), "cardLastFourDigit", "1234"), 200);
    }

    private void passedInspection() throws Exception {
        review("START", "Begin verification", null, 200);
        review("VERIFY", "Details verified", null, 200);
        review("SCHEDULE", null, LocalDate.now(), 200);
        review("PASS", "Inspection passed", null, 200);
    }

    @Test
    void paymentPersistsOnlyLastFourDigitsAndSimulationReference() throws Exception {
        JsonNode payment = pay().path("payment");
        assertEquals("1234", payment.path("cardLastFourDigit").asText());
        assertEquals("SUCCESS", payment.path("paymentStatus").asText());
        assertEquals("CARD", payment.path("paymentMethod").asText());
        assertTrue(payment.path("transactionReferenceId").asText().startsWith("SIM-"));
        assertTrue(payment.path("paidAt").isTextual());
        assertFalse(payment.has("cardNumber"));
        assertFalse(payment.has("cvv"));
    }

    @Test
    void paymentRetryReturnsSamePayment() throws Exception {
        long id = pay().path("payment").path("paymentId").asLong();
        assertEquals(id, pay().path("payment").path("paymentId").asLong());
    }

    @Test
    void concurrentPaymentRetriesCreateSinglePayment() throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> pay().path("payment").path("paymentId").asLong());
            var second = executor.submit(() -> pay().path("payment").path("paymentId").asLong());
            assertEquals(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void wrongPaymentAmountReturnsNamedError() throws Exception {
        JsonNode response = call("POST", "/api/applications/" + reference + "/payments",
                Map.of("amount", 1, "cardLastFourDigit", "1234"), 400);
        assertEquals("PAYMENT_AMOUNT_MISMATCH", response.path("code").asText());
        assertTrue(call("GET", "/api/applications/" + reference, null, 200).path("payment").isNull());
    }

    @Test
    void invalidPaymentFieldsReturnValidationErrors() throws Exception {
        assertEquals("VALIDATION_FAILED", call("POST", "/api/applications/" + reference + "/payments",
                Map.of("amount", -1, "cardLastFourDigit", "12xx"), 400).path("code").asText());
    }

    @Test
    void missingPaymentApplicationReturnsNamedError() throws Exception {
        assertEquals("APPLICATION_ID_NOT_FOUND", call("POST", "/api/applications/VRS-MISSING/payments",
                Map.of("amount", 100, "cardLastFourDigit", "1234"), 404).path("code").asText());
    }

    @Test
    void completeWorkflowIssuesOriginalRcAndUpdatesVehicle() throws Exception {
        pay();
        passedInspection();
        JsonNode approved = review("APPROVE", "Approved after inspection and payment", null, 200);
        JsonNode certificate = approved.path("certificate");
        assertEquals("APPROVED", approved.path("applicationStatus").asText());
        assertEquals("APPROVED", approved.path("finalResult").asText());
        assertEquals("VR" + approved.path("vehicleId").asLong(), certificate.path("registrationNumber").asText());
        assertEquals(ownerId.longValue(), certificate.path("registeredOwnerId").asLong());
        assertEquals(employeeId.longValue(), certificate.path("issuedByEmployeeId").asLong());
        assertEquals(LocalDate.now().toString(), certificate.path("issuedDate").asText());
        assertEquals(LocalDate.now().plusYears(15).toString(), certificate.path("validTill").asText());
        assertEquals(certificate.path("registrationNumber").asText(),
                approved.path("vehicle").path("registrationcertificateNumber").asText());
    }

    @Test
    void certificateEndpointReturnsPersistedCertificate() throws Exception {
        pay();
        passedInspection();
        String number = review("APPROVE", "Approved", null, 200).path("certificate").path("registrationNumber").asText();
        assertEquals(number, call("GET", "/api/applications/" + reference + "/certificate", null, 200)
                .path("registrationNumber").asText());
    }

    @Test
    void certificateBeforeApprovalReturnsNamedError() throws Exception {
        assertEquals("CERTIFICATE_NOT_FOUND", call("GET", "/api/applications/" + reference + "/certificate", null, 404)
                .path("code").asText());
    }

    @Test
    void approvalRequiresPassedInspection() throws Exception {
        pay();
        assertEquals("INSPECTION_NOT_PASSED", review("APPROVE", "Too early", null, 409).path("code").asText());
    }

    @Test
    void approvalRequiresSuccessfulPayment() throws Exception {
        passedInspection();
        assertEquals("PAYMENT_REQUIRED", review("APPROVE", "Payment missing", null, 409).path("code").asText());
    }

    @Test
    void failedInspectionCannotBeApproved() throws Exception {
        pay();
        review("START", null, null, 200);
        review("VERIFY", "Verified", null, 200);
        review("SCHEDULE", null, LocalDate.now(), 200);
        review("FAIL", "Inspection failed", null, 200);
        assertEquals("INSPECTION_NOT_PASSED", review("APPROVE", "Not passed", null, 409).path("code").asText());
    }

    @Test
    void correctionAndResubmissionPreserveReferenceAndFee() throws Exception {
        review("START", null, null, 200);
        review("CORRECTION", "Correct vehicle color", null, 200);
        Map<String, Object> corrected = new LinkedHashMap<>(vehicle());
        corrected.put("colorVariant", "White");
        JsonNode response = call("PUT", "/api/applications/" + reference + "/correction", corrected, 200);
        assertEquals(reference, response.path("applicationRefNo").asText());
        assertEquals("SUBMITTED", response.path("applicationStatus").asText());
        assertEquals("White", response.path("vehicle").path("colorVariant").asText());
        assertEquals(feeId.longValue(), response.path("feeRuleId").asLong());
    }

    @Test
    void correctionCannotChangeQuotedCategory() throws Exception {
        review("START", null, null, 200);
        review("CORRECTION", "Correct details", null, 200);
        Map<String, Object> corrected = new LinkedHashMap<>(vehicle());
        corrected.put("vehicleCategory", "TWO_WHEELER");
        assertEquals("INVALID_OPERATION", call("PUT", "/api/applications/" + reference + "/correction", corrected, 409)
                .path("code").asText());
    }

    @Test
    void correctionRequiresRemarks() throws Exception {
        review("START", null, null, 200);
        assertEquals("INVALID_OPERATION", review("CORRECTION", "", null, 409).path("code").asText());
    }

    @Test
    void verificationCannotSkipStart() throws Exception {
        assertEquals("INVALID_OPERATION", review("VERIFY", "Verified", null, 409).path("code").asText());
    }

    @Test
    void inspectionCannotSkipScheduling() throws Exception {
        review("START", null, null, 200);
        review("VERIFY", "Verified", null, 200);
        assertEquals("INVALID_OPERATION", review("PASS", "Passed", null, 409).path("code").asText());
    }

    @Test
    void futureAppointmentCannotBeRecordedAsPassed() throws Exception {
        review("START", null, null, 200);
        review("VERIFY", "Verified", null, 200);
        review("SCHEDULE", null, LocalDate.now().plusDays(1), 200);
        assertEquals("INVALID_OPERATION", review("PASS", "Too early", null, 409).path("code").asText());
    }

    @Test
    void pastAppointmentIsRejected() throws Exception {
        review("START", null, null, 200);
        review("VERIFY", "Verified", null, 200);
        assertEquals("INVALID_OPERATION", review("SCHEDULE", null, LocalDate.now().minusDays(1), 409).path("code").asText());
    }

    @Test
    void rejectionClosesApplicationAndBlocksPayment() throws Exception {
        assertEquals("REJECTED", review("REJECT", "Rejected with reason", null, 200).path("finalResult").asText());
        assertEquals("INVALID_OPERATION", call("POST", "/api/applications/" + reference + "/payments",
                Map.of("amount", new BigDecimal("1234.56"), "cardLastFourDigit", "1234"), 409).path("code").asText());
        assertEquals("INVALID_OPERATION", review("START", null, null, 409).path("code").asText());
    }

    @Test
    void rejectionRequiresRemarks() throws Exception {
        assertEquals("INVALID_OPERATION", review("REJECT", null, null, 409).path("code").asText());
    }

    @Test
    void unknownAndMissingReviewActionReturnBadRequest() throws Exception {
        assertEquals("INVALID_REQUEST", review("UNKNOWN", null, null, 400).path("code").asText());
        assertEquals("VALIDATION_FAILED", call("POST", "/api/applications/" + reference + "/review?employeeId=" + employeeId,
                Map.of(), 400).path("code").asText());
    }

    @Test
    void unknownEmployeeReturnsNamedError() throws Exception {
        assertEquals("EMPLOYEE_NOT_FOUND", call("POST", "/api/applications/" + reference + "/review?employeeId=" + Long.MAX_VALUE,
                Map.of("action", "START"), 404).path("code").asText());
    }

    @Test
    void employeeCreationHashesPasswordAndSupportsLogin() throws Exception {
        JsonNode login = call("POST", "/api/auth/login", Map.of("email", employeeEmail, "password", PASSWORD, "staff", true), 200);
        assertEquals(employeeId.longValue(), login.path("id").asLong());
        assertEquals("RTO_OFFICER", login.path("role").asText());
        assertFalse(login.has("password"));
        assertEquals("DUPLICATE_RECORD", call("POST", "/api/employees", employee(), 409).path("code").asText());
    }

    @Test
    void accountListsNeverReturnPasswordHashes() throws Exception {
        for (JsonNode owner : call("GET", "/api/owners", null, 200)) {
            assertFalse(owner.has("password"));
            assertFalse(owner.has("role"));
        }
        for (JsonNode employee : call("GET", "/api/employees", null, 200)) {
            assertFalse(employee.has("password"));
        }
    }

    @Test
    void ownerAccountStatusControlsLoginAndHasNoRole() throws Exception {
        call("PATCH", "/api/accounts/owners/" + ownerId, Map.of("status", "INACTIVE"), 200);
        assertEquals("INVALID_LOGIN", call("POST", "/api/auth/login",
                Map.of("email", prefix + "@example.com", "password", PASSWORD, "staff", false), 401).path("code").asText());
        assertEquals("INVALID_OPERATION", call("PATCH", "/api/accounts/owners/" + ownerId,
                Map.of("status", "ACTIVE", "role", "RTO_ADMIN"), 409).path("code").asText());
    }

    @Test
    void employeeRoleAndStatusUpdatesArePersisted() throws Exception {
        call("PATCH", "/api/accounts/employees/" + employeeId, Map.of("status", "ACTIVE", "role", "RTO_ADMIN"), 200);
        assertEquals("RTO_ADMIN", call("POST", "/api/auth/login", Map.of("email", employeeEmail, "password", PASSWORD, "staff", true), 200)
                .path("role").asText());
        call("PATCH", "/api/accounts/employees/" + employeeId, Map.of("status", "INACTIVE"), 200);
        call("POST", "/api/auth/login", Map.of("email", employeeEmail, "password", PASSWORD, "staff", true), 401);
    }

    @Test
    void invalidAccountKindAndMissingAccountsReturnNamedErrors() throws Exception {
        assertEquals("INVALID_REQUEST", call("PATCH", "/api/accounts/unknown/" + ownerId, Map.of("status", "ACTIVE"), 400).path("code").asText());
        assertEquals("OWNER_NOT_FOUND", call("PATCH", "/api/accounts/owners/" + Long.MAX_VALUE, Map.of("status", "ACTIVE"), 404).path("code").asText());
        assertEquals("EMPLOYEE_NOT_FOUND", call("PATCH", "/api/accounts/employees/" + Long.MAX_VALUE, Map.of("status", "ACTIVE"), 404).path("code").asText());
    }

    @Test
    void invalidEmployeeAndAccountRequestsReturnFieldErrors() throws Exception {
        assertEquals("VALIDATION_FAILED", call("POST", "/api/employees", Map.of(), 400).path("code").asText());
        assertEquals("VALIDATION_FAILED", call("PATCH", "/api/accounts/owners/" + ownerId, Map.of(), 400).path("code").asText());
    }

    @Test
    void reportsIncludePaymentAndAuditIncludesWorkflowHistory() throws Exception {
        pay();
        passedInspection();
        review("APPROVE", "Approved", null, 200);
        JsonNode report = call("GET", "/api/reports", null, 200);
        assertTrue(report.path("feeCollected").decimalValue().compareTo(new BigDecimal("1234.56")) >= 0);
        assertTrue(report.path("counts").path("APPROVED").asLong() >= 1);
        Set<String> actions = new HashSet<>();
        for (JsonNode audit : call("GET", "/api/audit", null, 200)) {
            if (reference.equals(audit.path("applicationRefNo").asText())) {
                actions.add(audit.path("action").asText());
                assertTrue(audit.path("createdAt").isTextual());
            }
        }
        assertTrue(actions.containsAll(List.of("SUBMITTED", "PAYMENT_RECORDED", "START", "VERIFY", "SCHEDULE", "PASS", "APPROVE")));
    }

    @Test
    void repeatedApprovalIsRejectedWithoutSecondCertificate() throws Exception {
        pay();
        passedInspection();
        String number = review("APPROVE", "Approved", null, 200).path("certificate").path("registrationNumber").asText();
        review("APPROVE", "Repeated approval", null, 409);
        assertEquals(number, call("GET", "/api/applications/" + reference + "/certificate", null, 200).path("registrationNumber").asText());
    }
}
