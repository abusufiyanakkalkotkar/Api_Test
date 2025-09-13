package api.post.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertTrue;

public class ChatWithDifferentAutomaticToken {
    private Playwright playwright;
    private APIRequestContext requestContext;

    // Store tokens for users
    private String user1Token;
    private String user2Token;

    @BeforeTest
    public void setup() {
        System.out.println("=== Setting up test environment ===");
        playwright = Playwright.create();
        APIRequest request = playwright.request();
        requestContext = request.newContext();

        // Generate tokens for both users
        generateUserTokens();
    }

    //Generate tokens for both users before running the test
    private void generateUserTokens() {
        try {
            System.out.println("Generating tokens for both users...");
            user1Token = generateUser1Token();
            user2Token = generateUser2Token();

            System.out.println("User1 (alam) token: " + user1Token);
            System.out.println("User2 (sufiyan) token: " + user2Token);
            System.out.println("Tokens generated successfully!");
        } catch (Exception e) {
            System.err.println("Failed to generate tokens: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unable to generate user tokens", e);
        }
    }

    //Generate token for User1 (alam)
    private String generateUser1Token() {
        System.out.println("Generating token for User1 (alam)...");

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "alam");
        loginData.put("password", "Test@123");

        APIResponse apiResponse = requestContext.post("https://api.ababillapp.online/v1/app/login",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Package-Name", "com.mosque.ababill")
                        .setData(loginData));
        System.out.println("User1 login response status: " + apiResponse.status());

        if (apiResponse.status() == 200) {
            String responseText = apiResponse.text();
            System.out.println("User1 full response: " + responseText);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(apiResponse.body());
                System.out.println("User1 JSON response structure: " + jsonResponse.toPrettyString());

                // Try to extract token from different possible locations
                JsonNode dataNode = jsonResponse.get("data");
                if (dataNode != null) {
                    JsonNode accessTokenNode = dataNode.get("access_token");
                    if (accessTokenNode != null) {
                        System.out.println("User1 login successful - token found in data.access_token");
                        return accessTokenNode.asText();
                    }
                }

                // Try direct access_token field
                JsonNode accessTokenNode = jsonResponse.get("access_token");
                if (accessTokenNode != null) {
                    System.out.println("User1 login successful - token found in access_token");
                    return accessTokenNode.asText();
                }

                // If we can't find token, throw detailed exception
                throw new RuntimeException("Unable to find access token in User1 response. Response structure: " + jsonResponse.toPrettyString());

            } catch (Exception e) {
                System.err.println("Error parsing User1 JSON response: " + e.getMessage());
                throw new RuntimeException("Failed to parse token for User1 from response: " + responseText, e);
            }
        } else {
            System.err.println("User1 login failed: " + apiResponse.text());
            throw new RuntimeException("Failed to generate token for User1. Status: " + apiResponse.status());
        }
    }

    //Generate token for User2 (sufiyan)
    private String generateUser2Token() {
        System.out.println("Generating token for User2 (sufiyan)...");

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "sufiyan");
        loginData.put("password", "Sufiyan@123");

        APIResponse apiResponse = requestContext.post("https://api.ababillapp.online/v1/app/login",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Package-Name", "com.mosque.ababill")
                        .setData(loginData));

        System.out.println("User2 login response status: " + apiResponse.status());

        if (apiResponse.status() == 200) {
            String responseText = apiResponse.text();
            System.out.println("User2 full response: " + responseText);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(apiResponse.body());
                System.out.println("User2 JSON response structure: " + jsonResponse.toPrettyString());

                // Try to extract token from different possible locations
                JsonNode dataNode = jsonResponse.get("data");
                if (dataNode != null) {
                    JsonNode accessTokenNode = dataNode.get("access_token");
                    if (accessTokenNode != null) {
                        System.out.println("User2 login successful - token found in data.access_token");
                        return accessTokenNode.asText();
                    }
                }

                // Try direct access_token field
                JsonNode accessTokenNode = jsonResponse.get("access_token");
                if (accessTokenNode != null) {
                    System.out.println("User2 login successful - token found in access_token");
                    return accessTokenNode.asText();
                }

                // If we can't find token, throw detailed exception
                throw new RuntimeException("Unable to find access token in User2 response. Response structure: " + jsonResponse.toPrettyString());

            } catch (Exception e) {
                System.err.println("Error parsing User2 JSON response: " + e.getMessage());
                throw new RuntimeException("Failed to parse token for User2 from response: " + responseText, e);
            }
        } else {
            System.err.println("User2 login failed: " + apiResponse.text());
            throw new RuntimeException("Failed to generate token for User2. Status: " + apiResponse.status());
        }
    }

    @Test(invocationCount = 15)
    public void userMessagingTest() throws IOException, InterruptedException {
        System.out.println("=== Starting User Messaging Test ===");

        // Verify tokens are available
        if (user1Token == null || user2Token == null) {
            throw new RuntimeException("Tokens not available for messaging test");
        }

        // Generate unique messages for each test run
        long timestamp = System.currentTimeMillis();
        Map<String, String> user1Message = new HashMap<>();
        user1Message.put("message", "Hello from user1 - Test#" + timestamp);

        Map<String, String> user2Message = new HashMap<>();
        user2Message.put("message", "Hello from user2 - Test#" + timestamp);

        URI uri = URI.create("wss://api.ababillapp.online/v1/app/dashboard/chat/ws/chat/46");

        CountDownLatch user1Latch = new CountDownLatch(2); // Connect + Receive message
        CountDownLatch user2Latch = new CountDownLatch(3); // Connect + Receive message + Send reply
        AtomicBoolean user1Received = new AtomicBoolean(false);
        AtomicBoolean user2Received = new AtomicBoolean(false);
        AtomicBoolean user2Replied = new AtomicBoolean(false);

        System.out.println("Setting up WebSocket clients...");

        WebSocketClient user2Client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("User2 connected to WebSocket");
                user2Latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                System.out.println("User2 received: " + message);
                user2Received.set(true);
                user2Latch.countDown();

                // User2 replies only once
                if (!user2Replied.get()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String replyData = objectMapper.writeValueAsString(user2Message);
                        this.send(replyData);
                        System.out.println("User2 sent reply: " + replyData);
                        user2Replied.set(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("User2 connection closed: " + reason);
                user2Latch.countDown();
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("User2 WebSocket error: " + ex.getMessage());
                ex.printStackTrace();
                user2Latch.countDown();
            }
        };

        WebSocketClient user1Client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("User1 connected to WebSocket");
                user1Latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                System.out.println("User1 received reply: " + message);
                user1Received.set(true);
                user1Latch.countDown();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("User1 connection closed: " + reason);
                user1Latch.countDown();
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("User1 WebSocket error: " + ex.getMessage());
                ex.printStackTrace();
                user1Latch.countDown();
            }
        };

        // Add authentication headers
        System.out.println("Adding authentication headers...");
        user2Client.addHeader("Authorization", "Bearer " + user2Token);
        user2Client.addHeader("Content-Type", "application/json");

        user1Client.addHeader("Authorization", "Bearer " + user1Token);
        user1Client.addHeader("Content-Type", "application/json");

        // Connect clients
        System.out.println("Connecting User2 to WebSocket...");
        boolean user2Connected = user2Client.connectBlocking(10, TimeUnit.SECONDS);
        System.out.println("User2 connection result: " + user2Connected);

        System.out.println("Connecting User1 to WebSocket...");
        boolean user1Connected = user1Client.connectBlocking(10, TimeUnit.SECONDS);
        System.out.println("User1 connection result: " + user1Connected);

        // Wait a bit for connections to establish
        System.out.println("Waiting for connections to establish...");
        Thread.sleep(2000);

        // User1 sends message only once
        System.out.println("User1 sending message...");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(user1Message);
        user1Client.send(jsonData);
        System.out.println("User1 sent message: " + jsonData);

        // Wait for communication to complete
        System.out.println("Waiting for message exchange to complete...");
        boolean user1Success = user1Latch.await(30, TimeUnit.SECONDS);
        boolean user2Success = user2Latch.await(30, TimeUnit.SECONDS);

        System.out.println("=== Test Results ===");
        System.out.println("User1 success: " + user1Success);
        System.out.println("User2 success: " + user2Success);
        System.out.println("User1 received reply: " + user1Received.get());
        System.out.println("User2 received message: " + user2Received.get());

        // Assertions
        assertTrue(user1Success, "User1 operation should complete within timeout");
        assertTrue(user2Success, "User2 operation should complete within timeout");
        assertTrue(user1Received.get(), "User1 should receive reply from user2");
        assertTrue(user2Received.get(), "User2 should receive message from user1");

        // Clean up
        System.out.println("Closing WebSocket connections...");
        user1Client.close();
        user2Client.close();

        System.out.println("=== User Messaging Test Completed ===");
    }

    @AfterTest
    public void tearDown() {
        System.out.println("=== Tearing down test environment ===");
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
        System.out.println("Test environment torn down successfully!");
    }
}
