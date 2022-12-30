package simple_methods;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapStatusCodeTest {
    @Test
    public void testFor200() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/map")
                .andReturn();
        assertEquals(200, response.statusCode(), "Unexpected status code");
    }

    @Test
    public void testFor404() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/map1")
                .andReturn();
        assertEquals(404, response.statusCode(), "Unexpected status code");
    }
}
