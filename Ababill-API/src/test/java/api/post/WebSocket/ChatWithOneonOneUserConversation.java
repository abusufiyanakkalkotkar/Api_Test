package api.post.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
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

public class ChatWithOneonOneUserConversation {
    Playwright playwright;
    Browser browser;
    Page page;
    APIRequest request;
    APIRequestContext requestContext;

    @BeforeTest
    public void setup(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
        request =  playwright.request();
        requestContext = request.newContext();
    }

    @Test(invocationCount = 5)
    public void userMessagingTest() throws IOException, InterruptedException {
        String user1Token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxODIiLCJ1c2VybmFtZSI6ImFsYW0iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQxMCwidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1Nzc0NjU0Mn0.5JDVt41yaoPajSaaPvlbz9wmi0limZ5uHMG8rAHDIkE";
        Map<String, String> user1Message = new HashMap<>();
        user1Message.put("message", "how are you");

        String user2Token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxNzEiLCJ1c2VybmFtZSI6InN1Zml5YW4iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQwMywidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1Nzc0NjU3Mn0.W27poSsGLL609Lf3ItGDbU5P3qaG_UdZkruac7CGoW8";
        Map<String, String> user2Message = new HashMap<>();
        user2Message.put("message", "i am fine what about you");

        URI uri = URI.create("wss://api.ababillapp.online/v1/app/dashboard/chat/ws/chat/46");

        CountDownLatch user1Latch = new CountDownLatch(2);
        CountDownLatch user2Latch = new CountDownLatch(2);
        AtomicBoolean user1Received = new AtomicBoolean(false);
        AtomicBoolean user2Received = new AtomicBoolean(false);

        WebSocketClient user2Client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("User2 connected to WebSocket");
                user2Latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                System.out.println("User2 received: " + message);
                user2Received.set(true);

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String replyData = objectMapper.writeValueAsString(user2Message);
                    this.send(replyData);
                    System.out.println("User2 sent reply: " + replyData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                user2Latch.countDown();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("User2 connection closed: " + reason);
                user2Latch.countDown();
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                user2Latch.countDown();
            }
        };

        WebSocketClient user1Client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
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
                ex.printStackTrace();
                user1Latch.countDown();
            }
        };

        user2Client.addHeader("Authorization", "Bearer " + user2Token);
        user2Client.addHeader("Content-Type", "application/json");

        user1Client.addHeader("Authorization", "Bearer " + user1Token);
        user1Client.addHeader("Content-Type", "application/json");

        user2Client.connectBlocking();
        user1Client.connectBlocking();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(user1Message);
        user1Client.send(jsonData);
        System.out.println("User1 sent message: " + jsonData);

        boolean user1Success = user1Latch.await(15, TimeUnit.SECONDS);
        boolean user2Success = user2Latch.await(15, TimeUnit.SECONDS);

        assertTrue(user1Success, "User1 operation should complete within timeout");
        assertTrue(user2Success, "User2 operation should complete within timeout");
        assertTrue(user1Received.get(), "User1 should receive reply from user2");
        assertTrue(user2Received.get(), "User2 should receive message from user1");

        user1Client.close();
        user2Client.close();
    }

    @AfterTest
    public void tearDown(){
        if (playwright != null) {
            playwright.close();
        }
    }
}
