import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class LongRedirectTest {

    @Test
    public void testLongRedirect() {

        boolean doRequest = true;
        String requestUrl = "https://playground.learnqa.ru/api/long_redirect";
        int numOfRedirects = 0;

        while (doRequest) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(requestUrl);

            if (response.getHeaders().hasHeaderWithName("Location")) {
                requestUrl = response.getHeader("Location");
                System.out.printf("\nRedirect link%d :%s", numOfRedirects+1, requestUrl);
                numOfRedirects += 1;
            } else {
                doRequest = false;
            }
        }

        System.out.printf("\nTotal number of redirects: %d", numOfRedirects);
    }
}
