package first_steps;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserAgentCheckTest {
    @ParameterizedTest
    @CsvSource({
            "'Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30', " +
                    "Mobile, No, Android",
            "'Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1', " +
                    "Mobile, Chrome, iOS",
            "'Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)', Googlebot, Unknown, Unknown",
            "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0', " +
                    "Web, Chrome, No",
            "'Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1', " +
                    "Mobile, No, iPhone"
    })
    public void testUserAgentCheck(String userAgent, String expectedPlatform, String expectedBrowser, String expectedDevice) {

        Response response = RestAssured
                .given()
                .header("User-Agent", userAgent)
                .when()
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check")
                .andReturn();

        String actualPlatform = response.jsonPath().getString("platform");
        String actualBrowser = response.jsonPath().getString("browser");
        String actualDevice = response.jsonPath().getString("device");

        assertEquals(200, response.statusCode(),
                "Unexpected status code");

        response.then().assertThat().body("$", hasKey("platform"));
        response.then().assertThat().body("$", hasKey("browser"));
        response.then().assertThat().body("$", hasKey("device"));

        assertAll("Checking combination of platform, browser, device in the response",
                () -> assertEquals(expectedPlatform, actualPlatform,
                        "The platform is not equal to the expected value"),
                () -> assertEquals(expectedBrowser, actualBrowser,
                        "The browser is not equal to the expected value"),
                () -> assertEquals(expectedDevice, actualDevice,
                        "The device is not equal to the expected value"));
    }
}
