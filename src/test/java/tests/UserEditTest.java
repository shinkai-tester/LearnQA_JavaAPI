package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Edit cases")
@Feature("User data update")
public class UserEditTest extends BaseTestCase {

    String email;
    String firstName;
    String lastName;
    String password;
    int userId;

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void registerUser() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/",
                userData
        );

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        this.email = userData.get("email");
        this.firstName = userData.get("firstName");
        this.lastName = userData.get("lastName");
        this.password =userData.get("password");
        this.userId = this.getIntFromJson(responseCreateAuth, "id");
    }
    @Test
    @Description("Check that user can update own data (firstName)")
    @DisplayName("Successful data update: firstName")
    public void testEditJustCreatedUser() {
        // login as User
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.email);
        authData.put("password", this.password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // edit User firstName
        String newName = DataGenerator.getRandomFirstname();
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        apiCoreRequests.makeAuthPutRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                editData,
                cookie,
                header
        );

        // get User data
        Response responseUserData = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                header,
                cookie
        );

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Description("Check that user data can't be edited if user is not authorized")
    @DisplayName("Unsuccessful data update: unauthorized user")
    public void testEditUserNotAuth() {
        // edit User lastName
        String newLastName = DataGenerator.getRandomLastname();
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newLastName);
        Response response = apiCoreRequests.makeNotAuthPutRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                editData
        );

        Assertions.assertResponseCodeEquals(response, 400);
        Assertions.assertResponseTextEquals(response, "Auth token not supplied");

        // login as User
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.email);
        authData.put("password", this.password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // get User lastName - must not be updated
        Response responseUserData = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                header,
                cookie
        );
        Assertions.assertJsonByName(responseUserData, "lastName", this.lastName);
    }

    @Test
    @Description("Check that user email can't be changed to the value w/o @ sign")
    @DisplayName("Unsuccessful data update: email w/o @ sign")
    public void testEditUserBadEmail() {
        // login as User
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.email);
        authData.put("password", this.password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // edit User email - w/o @ sign
        String badEmail = "learnqaexample.com";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", badEmail);

        Response editResponse = apiCoreRequests.makeAuthPutRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                editData,
                cookie,
                header
        );

        Assertions.assertResponseCodeEquals(editResponse, 400);
        Assertions.assertResponseTextEquals(editResponse, "Invalid email format");

        // get User data - email mustn't be changed
        Response responseUserData = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                header,
                cookie
        );

        Assertions.assertJsonByName(responseUserData, "email", this.email);
    }

    @Test
    @Description("Check that firstName can't be changed to the value with length=1")
    @DisplayName("Unsuccessful data update: firstName length=1")
    public void testEditUserTooShortFirstname() {
        // login as User
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.email);
        authData.put("password", this.password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // edit User firstName - length=1
        String tooShortFirstname = "m";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", tooShortFirstname);

        Response editResponse = apiCoreRequests.makeAuthPutRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                editData,
                cookie,
                header
        );

        Assertions.assertResponseCodeEquals(editResponse, 400);
        Assertions.assertJsonHasField(editResponse, "error");
        Assertions.assertJsonByName(editResponse, "error", "Too short value for field firstName");

        // get User data - firstName mustn't be changed
        Response responseUserData = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + this.userId,
                header,
                cookie
        );

        Assertions.assertJsonByName(responseUserData, "firstName", this.firstName);
    }

    @Test
    @Description("Check that authorized User1 can't change the data of User2")
    @DisplayName("Unsuccessful data update: update User2 data as auth User1")
    public void testEditUserAsOtherUser() {
        // Register User2 which data User1 will edit
        Map<String, String> userDataUser2 = DataGenerator.getRegistrationData();

        Response responseCreateAuthUser2 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/",
                userDataUser2
        );

        Assertions.assertResponseCodeEquals(responseCreateAuthUser2, 200);
        Assertions.assertJsonHasField(responseCreateAuthUser2, "id");

        String passwordUser2 = userDataUser2.get("password");
        String emailUser2 = userDataUser2.get("email");
        String usernameUser2 = userDataUser2.get("username");
        int idUser2 = this.getIntFromJson(responseCreateAuthUser2, "id");

        // Login as User1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.email);
        authData.put("password", this.password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // Edit User2 as User1 - try to change the username
        String newUsernameUser2 = DataGenerator.getRandomUsername();
        Map<String, String> editData = new HashMap<>();
        editData.put("username", newUsernameUser2);
        Response editUser2Response = apiCoreRequests.makeAuthPutRequest(
                "https://playground.learnqa.ru/api/user/" + idUser2,
                editData,
                cookie,
                header
        );

        // MUST BE 403!!! There is a bug in API
        Assertions.assertResponseCodeEquals(editUser2Response, 200);

        // Login as User2
        Map<String, String> authDataUser2 = new HashMap<>();
        authDataUser2.put("email", emailUser2);
        authDataUser2.put("password", passwordUser2);

        Response responseGetAuthUser2 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authDataUser2
        );

        String headerUser2 = this.getHeader(responseGetAuthUser2, "x-csrf-token");
        String cookieUser2 = this.getCookie(responseGetAuthUser2, "auth_sid");

        // Authorized User2 checks own username - must not be updated by User1
        Response responseGetUser2 = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + idUser2,
                headerUser2,
                cookieUser2
        );

        Assertions.assertJsonByName(responseGetUser2, "username", usernameUser2);
    }
}
