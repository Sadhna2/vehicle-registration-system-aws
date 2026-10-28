package com.nexturn.vehicleregistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 * Tests stage 2 through real Lambda proxy requests, JPA and isolated local MySQL.
 * Uses JUnit only, without Mockito. Removes only this test's owner, vehicles,
 * applications, audit entries and fee fixtures after each test.
 */
public class NewVehicleRegistrationApiTest {
    private static final ObjectMapper JSON = LambdaContainerHandler.getObjectMapper();
    private static StreamLambdaHandler handler;
    private final List<Long> feeIds = new ArrayList<>();
    private String prefix;
    private Long ownerId;
    private long feeId;

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
        prefix = "stage2-" + UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> owner = new LinkedHashMap<>();
        owner.put("firstName", "VehicleTest");
        owner.put("lastName", "Owner");
        owner.put("emailAddress", prefix + "@example.com");
        owner.put("phoneNumber", "9876543210");
        owner.put("password", "Stage2Owner123!");
        owner.put("dateOfBirth", "1997-01-10");
        owner.put("identityProofType", "PAN");
        owner.put("identityProofNumber", prefix.substring(0, 28));
        owner.put("address", "10 Test Road");
        owner.put("cityName", "Pune");
        owner.put("stateName", "Maharashtra");
        owner.put("pincode", "411001");
        ownerId = call("POST", "/api/auth/signup", null, owner, 200).path("id").asLong();
        feeId = fee("PRIVATE_CAR", LocalDate.now());
    }

    /** Deletes fixtures in foreign-key order and keeps unrelated database rows. */
    @AfterEach
    void removeFixtures() throws Exception {
        if (ownerId == null) {
            return;
        }
        try (Connection connection = database()) {
            execute(connection, "DELETE FROM audit_logs WHERE application_ref_no IN "
                    + "(SELECT application_ref_no FROM vehicle_registration_applications WHERE applicant_id=?)", ownerId);
            execute(connection, "DELETE FROM vehicle_registration_applications WHERE applicant_id=?", ownerId);
            execute(connection, "DELETE FROM vehicle_details WHERE current_owner_id=?", ownerId);
            execute(connection, "DELETE FROM owner_details WHERE owner_id=?", ownerId);
            for (Long id : feeIds) {
                execute(connection, "DELETE FROM registration_fee_rules WHERE fee_rule_id=?", id);
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

    private long fee(String category, LocalDate effective) throws Exception {
        try (Connection connection = database(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO registration_fee_rules (vehicle_category,application_type,fee_amount,effective_from) "
                        + "VALUES (?,'NEW',1234.56,?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category);
            statement.setDate(2, Date.valueOf(effective));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                long id = keys.getLong(1);
                feeIds.add(id);
                return id;
            }
        }
    }

    private Map<String, Object> vehicle() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("vehicleCategory", "PRIVATE_CAR");
        request.put("manufacturerName", "Test Manufacturer");
        request.put("modelName", "Test Model");
        request.put("chassisNumber", "ch-" + prefix);
        request.put("engineNumber", "en-" + prefix);
        request.put("fuelType", "PETROL");
        request.put("manufactureYear", Year.now().getValue());
        request.put("colorVariant", "Blue");
        return request;
    }

    private JsonNode call(String method, String path, Long owner, Object body, int status) throws Exception {
        AwsProxyRequestBuilder request = new AwsProxyRequestBuilder(path, method)
                .header("Accept", "application/json");
        if (owner != null) {
            request.queryString("ownerId", owner.toString());
        }
        if (body == null) {
            request.nullBody();
        } else {
            request.header("Content-Type", "application/json").body(JSON.writeValueAsString(body));
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(request.buildStream(), output, new MockLambdaContext());
        AwsProxyResponse response = JSON.readValue(output.toByteArray(), AwsProxyResponse.class);
        assertEquals(status, response.getStatusCode(), response.getBody());
        return JSON.readTree(response.getBody());
    }

    private JsonNode submit(Map<String, Object> request, int status) throws Exception {
        return call("POST", "/api/applications", ownerId, request, status);
    }

    @Test
    void submissionPersistsVehicleApplicationFeeAndAudit() throws Exception {
        JsonNode result = submit(vehicle(), 200);
        String reference = result.path("applicationRefNo").asText();
        assertTrue(reference.matches("VRS-[A-F0-9]{24}"));
        assertEquals(ownerId.longValue(), result.path("applicantId").asLong());
        assertEquals("NEW", result.path("applicationType").asText());
        assertEquals("SUBMITTED", result.path("applicationStatus").asText());
        assertEquals(feeId, result.path("feeRuleId").asLong());
        assertEquals("1234.56", result.path("payableAmount").asText());
        assertEquals("PENDING", result.path("inspectionStatus").asText());
        assertTrue(result.path("submittedDate").isTextual(), "Return ISO dates matching Swagger.");
        assertEquals("1997-01-10", result.path("applicant").path("dateOfBirth").asText());
        assertTrue(result.path("payment").isNull());
        assertTrue(result.path("certificate").isNull());
        assertFalse(result.path("applicant").has("password"));
        assertEquals(("CH-" + prefix).toUpperCase(), result.path("vehicle").path("chassisNumber").asText());
        try (Connection connection = database(); PreparedStatement statement = connection.prepareStatement(
                "SELECT action FROM audit_logs WHERE application_ref_no=?")) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals("SUBMITTED", rows.getString(1));
            }
        }
    }

    @Test
    void referenceRetrievesPersistedApplication() throws Exception {
        String reference = submit(vehicle(), 200).path("applicationRefNo").asText();
        JsonNode result = call("GET", "/api/applications/" + reference, null, null, 200);
        assertEquals(reference, result.path("applicationRefNo").asText());
        assertEquals(ownerId.longValue(), result.path("applicant").path("ownerId").asLong());
    }

    @Test
    void applicationListFiltersByOwner() throws Exception {
        submit(vehicle(), 200);
        assertEquals(1, call("GET", "/api/applications", ownerId, null, 200).size());
        assertEquals(0, call("GET", "/api/applications", Long.MAX_VALUE, null, 200).size());
    }

    @Test
    void vehicleListFiltersByCurrentOwner() throws Exception {
        submit(vehicle(), 200);
        assertEquals(1, call("GET", "/api/vehicles", ownerId, null, 200).size());
        assertEquals(0, call("GET", "/api/vehicles", Long.MAX_VALUE, null, 200).size());
    }

    @Test
    void missingOwnerReturnsNamedError() throws Exception {
        JsonNode result = call("POST", "/api/applications", Long.MAX_VALUE, vehicle(), 404);
        assertEquals("OWNER_NOT_FOUND", result.path("code").asText());
    }

    @Test
    void missingReferenceReturnsNamedError() throws Exception {
        JsonNode result = call("GET", "/api/applications/VRS-MISSING", null, null, 404);
        assertEquals("REFERENCE_NUMBER_NOT_FOUND", result.path("code").asText());
    }

    @Test
    void missingFeeRollsBackNewVehicle() throws Exception {
        Map<String, Object> request = vehicle();
        request.put("vehicleCategory", "MVMG");
        assertEquals("FEE_RULE_NOT_FOUND", submit(request, 404).path("code").asText());
        assertEquals(0, call("GET", "/api/vehicles", ownerId, null, 200).size());
    }

    @Test
    void futureManufactureYearIsRejected() throws Exception {
        Map<String, Object> request = vehicle();
        request.put("manufactureYear", Year.now().getValue() + 1);
        assertEquals("INVALID_OPERATION", submit(request, 409).path("code").asText());
        assertEquals(0, call("GET", "/api/vehicles", ownerId, null, 200).size());
    }

    @Test
    void duplicateChassisDoesNotCreateAnotherApplication() throws Exception {
        submit(vehicle(), 200);
        Map<String, Object> duplicate = vehicle();
        duplicate.put("engineNumber", "OTHER-" + prefix);
        assertEquals("DUPLICATE_RECORD", submit(duplicate, 409).path("code").asText());
        assertEquals(1, call("GET", "/api/applications", ownerId, null, 200).size());
    }

    @Test
    void duplicateEngineIsRejected() throws Exception {
        submit(vehicle(), 200);
        Map<String, Object> duplicate = vehicle();
        duplicate.put("chassisNumber", "OTHER-" + prefix);
        assertEquals("DUPLICATE_RECORD", submit(duplicate, 409).path("code").asText());
        assertEquals(1, call("GET", "/api/vehicles", ownerId, null, 200).size());
    }

    @Test
    void invalidVehicleFieldsReturnFieldErrors() throws Exception {
        assertEquals("VALIDATION_FAILED", submit(Map.of(), 400).path("code").asText());
    }

    @Test
    void missingOwnerParameterReturnsBadRequest() throws Exception {
        assertEquals("INVALID_REQUEST", call("POST", "/api/applications", null, vehicle(), 400)
                .path("code").asText());
    }

    @Test
    void futureFeeDoesNotReplaceCurrentlyEffectiveFee() throws Exception {
        fee("PRIVATE_CAR", LocalDate.now().plusDays(1));
        assertEquals(feeId, submit(vehicle(), 200).path("feeRuleId").asLong());
    }

    @Test
    void submittedApplicationCannotBeCorrected() throws Exception {
        String reference = submit(vehicle(), 200).path("applicationRefNo").asText();
        assertEquals("INVALID_OPERATION", call("PUT", "/api/applications/" + reference + "/correction",
                null, vehicle(), 409).path("code").asText());
    }

    @Test
    void feeListIncludesConfiguredRule() throws Exception {
        JsonNode fees = call("GET", "/api/fees", null, null, 200);
        boolean found = false;
        for (JsonNode fee : fees) {
            if (fee.path("feeRuleId").asLong() == feeId) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    void renewalFeeAndInvalidFeeAreRejected() throws Exception {
        Map<String, Object> request = Map.of("vehicleCategory", "PRIVATE_CAR", "applicationType", "RENEWAL",
                "feeAmount", 100, "effectiveFrom", LocalDate.now().toString());
        assertEquals("INVALID_OPERATION", call("POST", "/api/fees", null, request, 409).path("code").asText());
        assertEquals("VALIDATION_FAILED", call("POST", "/api/fees", null, Map.of(), 400).path("code").asText());
    }
}
