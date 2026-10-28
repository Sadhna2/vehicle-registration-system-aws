package com.nexturn.vehicleregistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexturn.vehicleregistration.util.PasswordUtil;

/**
 * Exercises registration and login through real API Gateway proxy events.
 * Uses JUnit, the Lambda adapter and a local MySQL test database without Mockito.
 * Test records have a unique prefix and are removed after each test.
 */
public class AuthenticationApiTest {
    private static final ObjectMapper JSON = LambdaContainerHandler.getObjectMapper();
    private static final String PASSWORD = "TestOwner123!";
    private static StreamLambdaHandler handler;
    private String prefix;
    private String email;

    @BeforeAll
    static void startApplication() {
        String url = System.getenv("DB_URL");
        assertNotNull(url, "Set DB_URL to the dedicated local MySQL test database.");
        String address = url.substring(0, url.contains("?") ? url.indexOf('?') : url.length());
        assertTrue(address.startsWith("jdbc:mysql://localhost:")
                || address.startsWith("jdbc:mysql://127.0.0.1:"),
                "Authentication tests may run only against local MySQL, never RDS.");
        assertTrue(address.endsWith("/nexturn26_aws_test"),
                "Use the isolated nexturn26_aws_test database for these tests.");
        handler = new StreamLambdaHandler();
    }

    @BeforeEach
    void uniqueRecords() {
        prefix = "aws-auth-" + UUID.randomUUID().toString().replace("-", "");
        email = prefix + "@example.com";
    }

    @AfterEach
    void removeTestRecords() throws Exception {
        if (prefix == null) {
            return;
        }
        try (Connection connection = database()) {
            for (String table : new String[] {"owner_details", "rta_employees"}) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE email_address LIKE ?")) {
                    statement.setString(1, prefix + "%");
                    statement.executeUpdate();
                }
            }
        }
    }

    private static Connection database() throws Exception {
        return DriverManager.getConnection(System.getenv("DB_URL"),
                System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    private Map<String, Object> ownerFields() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("firstName", "TestOwner");
        request.put("lastName", "Registration");
        request.put("emailAddress", email);
        request.put("phoneNumber", "9876543210");
        request.put("password", PASSWORD);
        request.put("dateOfBirth", "1997-01-10");
        request.put("identityProofType", "PAN");
        request.put("identityProofNumber", "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 25));
        request.put("address", "10 Test Road");
        request.put("cityName", "Pune");
        request.put("stateName", "Maharashtra");
        request.put("pincode", "411001");
        return request;
    }

    private AwsProxyResponse invoke(String path, String method, String body,
            Map<String, String> headers) throws Exception {
        AwsProxyRequestBuilder request = new AwsProxyRequestBuilder(path, method);
        headers.forEach(request::header);
        if (body == null) {
            request.nullBody();
        } else {
            request.body(body);
        }
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        handler.handleRequest(request.buildStream(), response, new MockLambdaContext());
        return JSON.readValue(response.toByteArray(), AwsProxyResponse.class);
    }

    private JsonNode post(String path, Object request, int expected) throws Exception {
        AwsProxyResponse response = invoke(path, "POST", JSON.writeValueAsString(request),
                Map.of("Accept", "application/json", "Content-Type", "application/json"));
        assertEquals(expected, response.getStatusCode(), response.getBody());
        return JSON.readTree(response.getBody());
    }

    private JsonNode signup() throws Exception {
        return post("/api/auth/signup", ownerFields(), 200);
    }

    private JsonNode login(String address, String password, boolean staff, int status) throws Exception {
        return post("/api/auth/login", Map.of("email", address, "password", password, "staff", staff), status);
    }

    private void updateStatus(String table, String address) throws Exception {
        try (Connection connection = database();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE " + table + " SET status='INACTIVE' WHERE email_address=?")) {
            statement.setString(1, address);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private String employee() throws Exception {
        String address = prefix + "-staff@example.com";
        try (Connection connection = database();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO rta_employees (first_name,last_name,email_address,phone_number,password,"
                        + "desgnation,role,status,date_of_joining) VALUES (?,?,?,?,?,?,?,?,?)")) {
            statement.setString(1, "TestEmployee");
            statement.setString(2, "Login");
            statement.setString(3, address);
            statement.setString(4, "9876543211");
            statement.setString(5, PasswordUtil.hash(PASSWORD));
            statement.setString(6, "OFFICER");
            statement.setString(7, "RTO_OFFICER");
            statement.setString(8, "ACTIVE");
            statement.setTimestamp(9, Timestamp.from(Instant.now()));
            assertEquals(1, statement.executeUpdate());
        }
        return address;
    }

    @Test
    void signupNormalizesEmailAndPersistsOnlyPasswordHash() throws Exception {
        Map<String, Object> fields = ownerFields();
        fields.put("emailAddress", email.toUpperCase(Locale.ROOT));
        JsonNode response = post("/api/auth/signup", fields, 200);
        assertTrue(response.path("id").asLong() > 0);
        assertEquals("OWNER", response.path("role").asText());
        assertEquals(3, response.size());
        try (Connection connection = database(); PreparedStatement statement = connection.prepareStatement(
                "SELECT email_address,password,status FROM owner_details WHERE owner_id=?")) {
            statement.setLong(1, response.path("id").asLong());
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(email, result.getString("email_address"));
                assertNotEquals(PASSWORD, result.getString("password"));
                assertTrue(PasswordUtil.matches(PASSWORD, result.getString("password")));
                assertEquals("ACTIVE", result.getString("status"));
            }
        }
    }

    @Test
    void ownerLoginReturnsAccountWithoutPasswordOrToken() throws Exception {
        JsonNode account = signup();
        JsonNode response = login(email.toUpperCase(Locale.ROOT), PASSWORD, false, 200);
        assertEquals(account.path("id"), response.path("id"));
        assertEquals("TestOwner", response.path("name").asText());
        assertEquals("OWNER", response.path("role").asText());
        assertFalse(response.has("password"));
        assertFalse(response.has("accessToken"));
    }

    @Test
    void duplicateOwnerEmailReturnsConflict() throws Exception {
        signup();
        assertEquals("DUPLICATE_RECORD", post("/api/auth/signup", ownerFields(), 409).path("code").asText());
    }

    @Test
    void duplicateIdentityProofReturnsConflict() throws Exception {
        Map<String, Object> fields = ownerFields();
        post("/api/auth/signup", fields, 200);
        fields.put("emailAddress", prefix + "-other@example.com");
        assertEquals("DUPLICATE_RECORD", post("/api/auth/signup", fields, 409).path("code").asText());
    }

    @Test
    void invalidRegistrationFieldsReturnFieldErrors() throws Exception {
        JsonNode response = post("/api/auth/signup", Map.of(), 400);
        assertEquals("VALIDATION_FAILED", response.path("code").asText());
        assertTrue(response.path("errors").isArray());
        assertTrue(response.path("errors").size() > 0);
    }

    @Test
    void incorrectAndUnknownCredentialsUseSameError() throws Exception {
        signup();
        JsonNode wrong = login(email, "IncorrectPassword!", false, 401);
        JsonNode unknown = login(prefix + "-missing@example.com", PASSWORD, false, 401);
        assertEquals("INVALID_LOGIN", wrong.path("code").asText());
        assertEquals(wrong.path("detail"), unknown.path("detail"));
    }

    @Test
    void inactiveOwnerCannotLogin() throws Exception {
        signup();
        updateStatus("owner_details", email);
        assertEquals("INVALID_LOGIN", login(email, PASSWORD, false, 401).path("code").asText());
    }

    @Test
    void employeeLoginUsesStoredRole() throws Exception {
        String address = employee();
        JsonNode response = login(address, PASSWORD, true, 200);
        assertEquals("RTO_OFFICER", response.path("role").asText());
        assertEquals("TestEmployee", response.path("name").asText());
        assertFalse(response.has("password"));
    }

    @Test
    void inactiveEmployeeCannotLogin() throws Exception {
        String address = employee();
        updateStatus("rta_employees", address);
        assertEquals("INVALID_LOGIN", login(address, PASSWORD, true, 401).path("code").asText());
    }

    @Test
    void missingStaffSelectorReturnsValidationError() throws Exception {
        assertEquals("VALIDATION_FAILED", post("/api/auth/login",
                Map.of("email", email, "password", PASSWORD), 400).path("code").asText());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        AwsProxyResponse response = invoke("/api/auth/signup", "POST", "{bad",
                Map.of("Content-Type", "application/json"));
        assertEquals(400, response.getStatusCode());
        assertEquals("INVALID_REQUEST", JSON.readTree(response.getBody()).path("code").asText());
    }

    @Test
    void unsupportedIdentityProofTypeReturnsBadRequest() throws Exception {
        Map<String, Object> request = ownerFields();
        request.put("identityProofType", "UNSUPPORTED");
        assertEquals("INVALID_REQUEST", post("/api/auth/signup", request, 400).path("code").asText());
    }

    @Test
    void localReactCorsPreflightIsSupported() throws Exception {
        AwsProxyResponse response = invoke("/api/auth/login", "OPTIONS", null,
                Map.of("Origin", "http://localhost:3000", "Access-Control-Request-Method", "POST",
                        "Access-Control-Request-Headers", "content-type"));
        assertEquals(200, response.getStatusCode(), response.getBody());
        assertEquals("http://localhost:3000",
                response.getMultiValueHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    void passwordHashesUseDifferentSaltsAndRejectMalformedValues() {
        String first = PasswordUtil.hash(PASSWORD);
        String second = PasswordUtil.hash(PASSWORD);
        assertNotEquals(first, second);
        assertTrue(PasswordUtil.matches(PASSWORD, first));
        assertFalse(PasswordUtil.matches("WrongPassword!", first));
        assertFalse(PasswordUtil.matches(PASSWORD, "not-a-valid-hash"));
    }
}
