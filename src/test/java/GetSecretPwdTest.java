import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GetSecretPwdTest {
    @Test
    public void testGetSecretPwd() throws IOException {
        // read Top 25 most common passwords by year according to SplashData from wiki
        Document doc = Jsoup.parse(new URL("https://en.wikipedia.org/wiki/List_of_the_most_common_passwords"), 5000);

        Elements elems = doc.select("#mw-content-text > div.mw-parser-output > table:nth-child(12) > tbody > tr > td").not("td:first-child");

        HashSet<String> passwords = new HashSet<String>();

        for (Element e : elems) {
            passwords.add(e.text().replace("[a]", ""));
        }

        // guess the right password
        for (String pwd : passwords) {
            Map<String, Object> body = new HashMap<>();
            body.put("login", "super_admin");
            body.put("password", pwd);
            Response getCookieResponse = RestAssured
                    .given()
                    .body(body)
                    .when()
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String auth_cookie = getCookieResponse.getCookie("auth_cookie");

            Map<String, String> cookies = new HashMap<>();
            if (auth_cookie != null) {
                cookies.put("auth_cookie", auth_cookie);
            }

            Response checkCookieResponse = RestAssured
                    .given()
                    .cookies(cookies)
                    .when()
                    .post("https://playground.learnqa.ru/api/check_auth_cookie")
                    .andReturn();

            if (!checkCookieResponse.asString().equals("You are NOT authorized")) {
                System.out.println(checkCookieResponse.asString());
                System.out.printf("The password for the login super_admin is %s", pwd);
                break;
            }
        }
    }
}

