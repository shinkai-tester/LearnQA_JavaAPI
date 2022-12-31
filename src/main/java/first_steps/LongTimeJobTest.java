package first_steps;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LongTimeJobTest {
    @Test
    public void testLongTimeJob() throws InterruptedException {

        JsonPath createTaskResponse = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        createTaskResponse.prettyPrint();

        int seconds = createTaskResponse.getInt("seconds");
        String token = createTaskResponse.getString("token");
        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readyTaskTime = now.plusSeconds(seconds);

        if (LocalDateTime.now().isBefore(readyTaskTime)) {
            JsonPath beforeTaskReadyResponse = RestAssured
                    .given()
                    .queryParams(params)
                    .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                    .jsonPath();

            beforeTaskReadyResponse.prettyPrint();

            assertEquals("Job is NOT ready", beforeTaskReadyResponse.get("status"), "Status is NOT ok (check before the task is ready)");
        }

        TimeUnit.SECONDS.sleep(seconds);

        JsonPath taskReadyResponse = RestAssured
                .given()
                .queryParams(params)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        taskReadyResponse.prettyPrint();

        assertEquals("Job is ready", taskReadyResponse.get("status"), "Status is NOT ok (check after the task is ready)");
        assertNotNull(taskReadyResponse.get("result"));
    }
}
