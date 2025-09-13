package Ababill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SufiyanPostApi {
    Playwright playwright;
    APIRequest request;
    APIRequestContext requestContext;

    @BeforeTest
    public void setup(){
        playwright = Playwright.create();
        request =  playwright.request();
        requestContext = request.newContext();
    }


    @Test
    public void UserTest() throws IOException {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "sufiyan");
        loginData.put("password", "Sufiyan@123");


        APIResponse apiResponse = requestContext.post("https://api.ababillapp.online/v1/app/login",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Package-Name", "com.mosque.ababill")
                .setData(loginData));
        int StatusCode = apiResponse.status();
        System.out.println("Status Code: " + StatusCode);
        /*Assert.assertEquals(StatusCode, 200);
        Assert.assertEquals(apiResponse.ok(), true);*/

        String StatusResText = apiResponse.statusText();
        System.out.println("Status Text: " + StatusResText);

        System.out.println("---print api response wIth plain test---");
        System.out.println(apiResponse.text());

        System.out.println("---print api jsone response---");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(apiResponse.body());
        String jsonPrettyResponse = jsonResponse.toPrettyString();
        System.out.println("JSON Response: " + jsonPrettyResponse);
    }

    @AfterTest
    public void tearDown(){
        requestContext.dispose();
        playwright.close();
    }
}