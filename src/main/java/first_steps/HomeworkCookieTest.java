package first_steps;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeworkCookieTest {
    @Test
    public void testHomeworkCookie() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        String expectedCookieName = "HomeWork";
        String expectedCookieValue = "hw_value";

        assertEquals(200, response.statusCode(),
                "Unexpected status code");
        assertTrue(response.getCookies().containsKey(expectedCookieName),
                "Response doesn't have cookie with name '" + expectedCookieName + "'");
        assertEquals(expectedCookieValue, response.getCookie(expectedCookieName),
                "Wrong value of the cookie " + expectedCookieName);
    }
}
