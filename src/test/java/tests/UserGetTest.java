package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Get user cases")
@Feature("Get user details")
public class UserGetTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @Description("Check that it is possible to get only username of the user if you are not authorized")
    @DisplayName("Get user details as not authorized user")
    public void testGetUserDataNotAuth() {
        Response responseUserData = apiCoreRequests.makeNotAuthGetRequest(
                "https://playground.learnqa.ru/api/user/2"
        );

        String[] missingFields = {"firstName", "lastName", "email"};

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNoFields(responseUserData, missingFields);
    }

    @Test
    @Description("Check that logged in user can get own data (username, email, firstName, lastName)")
    @DisplayName("Get own details when you are authorized")
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/2",
                header, cookie);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Test
    @Description("Check that authorized User1 can see only the username of User2")
    @DisplayName("Get User2 details as auth User1")
    public void testGetUserDetailsAsAnotherUser() {
        // Prepare data: register User1 for auth who will get data of User2
        Map<String, String> dataUser1 = DataGenerator.getRegistrationData();
        Response responseRegisterUser1 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/", dataUser1
        );

        Assertions.assertResponseCodeEquals(responseRegisterUser1, 200);
        Assertions.assertJsonHasField(responseRegisterUser1, "id");
        String emailUser1 = dataUser1.get("email");
        String passwordUser1 = dataUser1.get("password");
        String idUser1 = responseRegisterUser1.jsonPath().getString("id");

        // Prepare data: register User2 which data User1 will get
        Map<String, String> dataUser2 = DataGenerator.getRegistrationData();
        Response responseRegisterUser2 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/", dataUser2
        );

        Assertions.assertResponseCodeEquals(responseRegisterUser2, 200);
        Assertions.assertJsonHasField(responseRegisterUser2, "id");
        String usernameUser2 = dataUser2.get("username");
        String idUser2 = responseRegisterUser2.jsonPath().getString("id");

        // Login as User1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", emailUser1);
        authData.put("password", passwordUser1);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData
        );

        Assertions.assertJsonHasField(responseGetAuth, "user_id");
        Assertions.assertJsonByName(responseGetAuth, "user_id", idUser1);
        String token = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // Get User2 data as authorized User1
        Response responseGetUser2Data = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + idUser2,
                token, cookie
        );

        String[] missingFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasField(responseGetUser2Data, "username");
        Assertions.assertJsonHasNoFields(responseGetUser2Data, missingFields);
        Assertions.assertJsonByName(responseGetUser2Data, "username", usernameUser2);
    }
}
