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

import static org.junit.jupiter.api.Assertions.assertAll;

@Epic("Deletion cases")
@Feature("User deletion")
public class UserDeleteTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    private final Map<String, String> superUser = new HashMap<String, String>() {{
        put("email","vinkotov@example.com");
        put("password","1234");
    }};

    int superUserId = 2;

    @Test
    @Description("Check the deletion of the user with specific IDs")
    @DisplayName("Unsuccessful user deletion: ID=2")
    public void testDeleteUserWithIdTwo() {
        // Login as User with id=2
        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                superUser);

        Assertions.assertResponseCodeEquals(responseGetAuth, 200);
        Assertions.assertJsonHasField(responseGetAuth, "user_id");

        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        String header = this.getHeader(responseGetAuth, "x-csrf-token");

        // Try to delete User with id=2
        Response responseDelUser = apiCoreRequests.makeDeleteRequest(
                "https://playground.learnqa.ru/api/user/" + superUserId,
                cookie, header
        );

        Assertions.assertResponseCodeEquals(responseDelUser, 400);
        Assertions.assertResponseTextEquals(
                responseDelUser,
                "Please, do not delete test users with ID 1, 2, 3, 4 or 5."
        );
    }

    @Test
    @Description("Check that the authorized user can delete own data")
    @DisplayName("Successful user deletion")
    public void testDeleteUser() {
        // New User registration
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseNewUser = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/",
                userData
        );

        Assertions.assertResponseCodeEquals(responseNewUser, 200);
        Assertions.assertJsonHasField(responseNewUser, "id");

        String email = userData.get("email");
        String password = userData.get("password");
        int userId = this.getIntFromJson(responseNewUser, "id");

        // Login as new User
        Map<String, String> authData = new HashMap<>();
        authData.put("email", email);
        authData.put("password", password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // User deletion
        Response responseDelUser = apiCoreRequests.makeDeleteRequest(
                "https://playground.learnqa.ru/api/user/" + userId,
                cookie, header
        );

        Assertions.assertResponseCodeEquals(responseDelUser, 200);

        // Authorized superuser tries to get the data of the deleted user
        Response responseGetAuthSuper = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                superUser);

        Assertions.assertResponseCodeEquals(responseGetAuthSuper, 200);
        Assertions.assertJsonHasField(responseGetAuthSuper, "user_id");

        String cookieSuper = this.getCookie(responseGetAuthSuper, "auth_sid");
        String headerSuper = this.getHeader(responseGetAuthSuper, "x-csrf-token");

        Response responseGetDelUser = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + userId,
                headerSuper, cookieSuper
        );

        Assertions.assertResponseCodeEquals(responseGetDelUser, 404);
        Assertions.assertResponseTextEquals(responseGetDelUser, "User not found");

        // Try to log in with data of the deleted user
        Response responseLoginAsDeleted = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        Assertions.assertResponseCodeEquals(responseLoginAsDeleted, 400);
        Assertions.assertResponseTextEquals(responseLoginAsDeleted, "Invalid username/password supplied");
    }

    @Test
    @Description("Check that the user can't delete other user")
    @DisplayName("Deletion by other user")
    public void testDeleteUserByOther() {
        // Register User1 who tries to delete User2
        Map<String, String> user1Data = DataGenerator.getRegistrationData();
        Response responseUser1 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/",
                user1Data
        );

        Assertions.assertResponseCodeEquals(responseUser1, 200);
        Assertions.assertJsonHasField(responseUser1, "id");
        String user1Email = user1Data.get("email");
        String user1Password = user1Data.get("password");

        // Register User2
        Map<String, String> user2Data = DataGenerator.getRegistrationData();
        Response responseUser2 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/",
                user2Data
        );

        Assertions.assertResponseCodeEquals(responseUser2, 200);
        Assertions.assertJsonHasField(responseUser2, "id");
        int user2Id = this.getIntFromJson(responseUser2, "id");
        String user2Email = user2Data.get("email");
        String user2Password = user2Data.get("password");
        String user2Username = user2Data.get("username");
        String user2Firstname = user2Data.get("firstName");
        String user2Lastname = user2Data.get("lastName");

        // Login as User1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", user1Email);
        authData.put("password", user1Password);

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // User1 tries to delete User2
        Response responseDelUser = apiCoreRequests.makeDeleteRequest(
                "https://playground.learnqa.ru/api/user/" + user2Id,
                cookie, header
        );

        // BUG: should be 403 status code, it is not permitted to delete other user
        Assertions.assertResponseCodeEquals(responseDelUser, 200);

        // Login as User2 to check own data
        Map<String, String> authData2 = new HashMap<>();
        authData2.put("email", user2Email);
        authData2.put("password", user2Password);

        Response responseGetAuth2 = apiCoreRequests.makePostRequest(
                "https://playground.learnqa.ru/api/user/login",
                authData2);

        String header2 = this.getHeader(responseGetAuth2, "x-csrf-token");
        String cookie2 = this.getCookie(responseGetAuth2, "auth_sid");

        // User2 checks his/her data
        Response responseUser2Data = apiCoreRequests.makeGetRequest(
                "https://playground.learnqa.ru/api/user/" + user2Id,
                header2, cookie2);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertResponseCodeEquals(responseUser2Data, 200);
        Assertions.assertJsonHasFields(responseUser2Data, expectedFields);
        assertAll("Checking actual and expected username, firstName, lastName and email",
                () -> Assertions.assertJsonByName(responseUser2Data, expectedFields[0], user2Username),
                () -> Assertions.assertJsonByName(responseUser2Data, expectedFields[1], user2Firstname),
                () -> Assertions.assertJsonByName(responseUser2Data, expectedFields[2], user2Lastname),
                () -> Assertions.assertJsonByName(responseUser2Data, expectedFields[3], user2Email)
        );
    }
}
