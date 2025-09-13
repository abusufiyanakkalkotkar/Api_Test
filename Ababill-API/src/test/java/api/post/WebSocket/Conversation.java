// 添加依赖到 pom.xml
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class Conversation {
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

    @Test
    public void UserconversationTest() throws IOException {
        APIResponse apiResponse = requestContext.get("https://api.ababillapp.online/v1/app/dashboard/chat/ws/conversations",
                RequestOptions.create()
                        .setHeader("Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxODIiLCJ1c2VybmFtZSI6ImFsYW0iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQxMCwidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1NzU3MjgyMX0.qHw1oVDQaG9BiysFVuvfigXsBB2gfXxnNsEYW8vrFKA"));
        int StatusCode = apiResponse.status();
        System.out.println("Status Code: " + StatusCode);

        String StatusResText = apiResponse.statusText();
        System.out.println("Status Text: " + StatusResText);

        System.out.println("---print api response with plain text---");
        System.out.println(apiResponse.text());

        System.out.println("---print api json response---");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(apiResponse.body());
        String jsonPrettyResponse = jsonResponse.toPrettyString();
        System.out.println("JSON Response: " + jsonPrettyResponse);
    }

    @Test
    public void webSocketTest() {
        page.navigate("https://your-app-url-that-uses-websocket.com");

        WebSocket webSocket = page.waitForWebSocket(() -> {
            page.click("button#connect-ws"); // 示例按钮
        });

        System.out.println("WebSocket URL: " + webSocket.url());

        webSocket.onFrameReceived(frame -> {
            System.out.println("Received message: " + frame.text());
        });

        webSocket.onFrameSent(frame -> {
            System.out.println("Sent message: " + frame.text());
        });


        WebSocketFrame frame = webSocket.waitForFrameReceived(
            new WebSocket.WaitForFrameReceivedOptions()
                .setPredicate(f -> f.text().contains("expected response")),
            () -> {
                page.click("button#send-message");
            }
        );

        assertTrue(frame.text().contains("expected response"), "Should receive expected response");
    }
    @Test
    public void directWebSocketTest() throws Exception {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxODIiLCJ1c2VybmFtZSI6ImFsYW0iLCJyb2xlX2lkIjo0LCJtb3NxdWVfaWQiOjQxMCwidG9rZW5fdHlwZSI6IkFjY2VzcyIsImV4cCI6MTc1NzU3MzQ0NH0.5WriCuq-sk71MrfySq6CKYAZRbwQAO2tt7jXzueULSA";

        URI uri = URI.create("wss://api.ababillapp.online/v1/app/dashboard/chat/ws/conversations");

        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("Received: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        client.addHeader("Authorization", "Bearer " + jwtToken);

        client.connectBlocking();

        client.send("Hello Server");

        Thread.sleep(5000);

        client.close();
    }

    @AfterTest
    public void tearDown(){
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}


