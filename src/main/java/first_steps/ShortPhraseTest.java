package first_steps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ShortPhraseTest {

    @ParameterizedTest
    @ValueSource(strings = {"Fourteen chars", "Exact fifteen:)", "Sixteen symbols!", ""})
    public void testShortPhrase(String text) {

        assertTrue(text.length() < 15, "String '" + text + "' has length=" + text.length()
                + ". Should be less than 15!");
        assertFalse(text.isEmpty(), "There is an empty string!");
    }
}

