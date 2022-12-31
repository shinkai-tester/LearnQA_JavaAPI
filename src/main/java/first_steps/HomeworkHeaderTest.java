package first_steps;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeworkHeaderTest {

    @Test
    public void testHomeworkHeader() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        String expectedHeaderName = "x-secret-homework-header";
        String expectedHeaderValue = "Some secret value";

        assertEquals(200, response.statusCode(),
                "Unexpected status code");
        assertTrue(response.getHeaders().hasHeaderWithName(expectedHeaderName),
                "Response doesn't have header with name '" + expectedHeaderName + "'");
        assertEquals(expectedHeaderValue, response.getHeader(expectedHeaderName),
                "Wrong value of the header " + expectedHeaderValue);
    }
}
