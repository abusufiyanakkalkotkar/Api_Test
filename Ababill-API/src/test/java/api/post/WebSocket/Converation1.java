package api.post.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
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

public class Converation1 {
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
    public void UserTest() throws IOException, InterruptedException {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("message", "my name is abzmobz");
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxODIiLCJ1c2VybmFtZSI6ImFsYW0iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQxMCwidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1NzczOTA2MH0.6deVTy3PD1otvOR2dN_iuzYP7zCLRFbT0_o2OOOrCwE";
        URI uri = URI.create("wss://api.ababillapp.online/v1/app/dashboard/chat/ws/chat/46");


        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean messageReceived = new AtomicBoolean(false);

        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket");
            }
            @Override
            public void onMessage(String message) {
                System.out.println("Received: " + message);
                messageReceived.set(true);
                latch.countDown();
            }
            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
                latch.countDown();
            }
            @Override
            public void onError(@org.jetbrains.annotations.NotNull Exception ex) {
                ex.printStackTrace();
                latch.countDown();
            }
        };

        client.addHeader("Authorization", "Bearer " + jwtToken);
        client.addHeader("Content-Type", "application/json");
        client.connectBlocking();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(loginData);
        client.send(jsonData);
        System.out.println("Sent JSON data: " + jsonData);

        latch.await(10, TimeUnit.SECONDS);
        assertTrue(messageReceived.get(), "Should receive response from server");
        client.close();
    }

    @Test(invocationCount = 5)
    public void UserTest2() throws IOException, InterruptedException {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("message", "my name is abusufiyan");
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxNzEiLCJ1c2VybmFtZSI6InN1Zml5YW4iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQwMywidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1NzczOTQ1NX0.bfzWkJjetTKspiGu4xTH0MWq26ho8e8qbcsyV52gjvc";
        URI uri = URI.create("wss://api.ababillapp.online/v1/app/dashboard/chat/ws/chat/46");


        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean messageReceived = new AtomicBoolean(false);

        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket");
            }
            @Override
            public void onMessage(String message) {
                System.out.println("Received: " + message);
                messageReceived.set(true);
                latch.countDown();
            }
            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
                latch.countDown();
            }
            @Override
            public void onError(@org.jetbrains.annotations.NotNull Exception ex) {
                ex.printStackTrace();
                latch.countDown();
            }
        };

        client.addHeader("Authorization", "Bearer " + jwtToken);
        client.addHeader("Content-Type", "application/json");
        client.connectBlocking();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(loginData);
        client.send(jsonData);
        System.out.println("Sent JSON data: " + jsonData);

        latch.await(10, TimeUnit.SECONDS);
        assertTrue(messageReceived.get(), "Should receive response from server");
        client.close();
    }

    @BeforeTest
    public void tearDown(){
        playwright.close();
    }
}