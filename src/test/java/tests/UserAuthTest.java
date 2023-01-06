package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("DEMMGT-123 Open user API basic methods")
@Feature("Authorization cases")
public class UserAuthTest extends BaseTestCase {

    String cookie;
    String header;
    int userIdOnAuth;

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    @Step("Login as user with email 'vinkotov@example.com'")
    public void loginUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData
        );

        this.cookie = this.getCookie(responseGetAuth, "auth_sid");
        this.header = this.getHeader(responseGetAuth, "x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth, "user_id");
    }

    @Test
    @Story("STORY-1")
    @Description("This test successfully authorizes user by email and password")
    @DisplayName("Successful user authorization")
    @Severity(SeverityLevel.BLOCKER)
    public void testAuthUser() {
        Response responseCheckAuth = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/auth",
                this.header,
                this.cookie
        );

        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @Description("This test checks authorization status w/o sending auth cookie or token")
    @DisplayName("Unsuccessful user authorization: w/o sending auth cookie or token")
    @Story("STORY-2")
    @Severity(SeverityLevel.CRITICAL)
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {
        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(
                    "https://playground.learnqa.ru/api/user/auth",
                    this.cookie
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(
                    "https://playground.learnqa.ru/api/user/auth",
                    this.header
            );
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is not known: " + condition);
        }
    }
}
