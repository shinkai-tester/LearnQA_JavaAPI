package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Registration cases")
@Feature("Registration")
public class UserRegisterTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @Description("Check that it is not possible to register a user with an existing email")
    @DisplayName("Unsuccessful user registration: existing email")
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    @Description("Check that it is possible to register user with email, password, username, firstName and lastName")
    @DisplayName("Successful user registration")
    public void testCreateUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @Description("Check that it is not possible to register a user with an email w/o @ sign")
    @DisplayName("Unsuccessful user registration: email w/o @ sign")
    public void testCreateUserBadEmail() {
        String badEmail = "aleksandraexample.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", badEmail);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"username", "firstName", "lastName", "email", "password"})
    @Description("Check that it is not possible to register a user if one of the parameters is missing")
    @DisplayName("Unsuccessful user registration: missing parameter")
    public void testCreateWithoutOneParam(String parameter) {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.remove(parameter);

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(
                responseCreateAuth, "The following required params are missed: " + parameter);
    }

    @Test
    @Description("Check that it is not possible to register a user with firstName length = 1 char")
    @DisplayName("Unsuccessful user registration: firstName length=1")
    public void testCreateUserWithTooShortFirstname() {
        String badFirstname = "S";

        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", badFirstname);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too short");
    }

    @Test
    @Description("Check that it is not possible to register a user with length of firstName > 250 symbols")
    @DisplayName("Unsuccessful user registration: firstName length > 250")
    public void testCreateUserWithTooLongFirstname() {
        String badFirstname = RandomStringUtils.randomAlphabetic(251);

        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", badFirstname);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest("https://playground.learnqa.ru/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too long");
    }
}
