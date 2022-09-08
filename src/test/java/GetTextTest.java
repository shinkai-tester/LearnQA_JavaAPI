import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class GetTextTest {
    @Test
    public void testGetText(){
        String response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn()
                .asString();
        System.out.println(response);
    }
}
