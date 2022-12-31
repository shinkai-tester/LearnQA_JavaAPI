package first_steps;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

public class GetJsonHwTest {

    @Test
    public void testGetJsonHw(){

        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String secondMessage = response.getString("messages[1].message");
        System.out.print(secondMessage);

    }
}
