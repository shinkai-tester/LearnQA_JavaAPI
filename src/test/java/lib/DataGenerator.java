package lib;

import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Map;

public class DataGenerator {
    static Faker faker = new Faker();

    public static String getRandomEmail() {
        return faker.internet().safeEmailAddress();
    }

    public static String getRandomUsername() {
        return faker.name().username();
    }

    public static String getRandomFirstname() {
        return faker.name().firstName();
    }

    public static String getRandomLastname() {
        return faker.name().lastName();
    }

    public static Map<String, String> getRegistrationData() {
        Map<String, String> data = new HashMap<>();
        data.put("email", getRandomEmail());
        data.put("username", getRandomUsername());
        data.put("firstName", getRandomFirstname());
        data.put("lastName", getRandomLastname());
        data.put("password", "123");
        return data;
    }

    public static Map<String, String> getRegistrationData(Map<String, String> nonDefaultValues) {
        Map<String, String> defaultValues = DataGenerator.getRegistrationData();

        Map<String, String> userData = new HashMap<>();
        String[] keys = {"username", "firstName", "lastName", "email", "password"};
        for(String key : keys) {
            if (nonDefaultValues.containsKey(key)) {
                userData.put(key, nonDefaultValues.get(key));
            } else {
                userData.put(key, defaultValues.get(key));
            }
        }
        return userData;
    }
}
