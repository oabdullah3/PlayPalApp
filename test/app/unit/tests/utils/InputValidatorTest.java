package app.unit.tests.utils;

import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {
    private final InputStream originalIn = System.in;

    @AfterEach
    void tearDown() throws Exception {
        System.setIn(originalIn);
        injectScanner(new Scanner(System.in));
    }

    private void injectScanner(Scanner s) throws Exception {
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, s);
    }

    private void provideInput(String data) throws Exception {
        injectScanner(new Scanner(new ByteArrayInputStream(data.getBytes())));
    }

    @Test
    void testReadString() throws Exception {
        provideInput("\n  \nValid\n"); // Skip empty/whitespace
        assertEquals("Valid", InputValidator.readString("Prompt"));
    }

    @Test
    void testReadOptional() throws Exception {
        provideInput("\nValid\n");
        assertEquals("", InputValidator.readOptionalString("Prompt")); // Empty input
        assertEquals("Valid", InputValidator.readOptionalString("Prompt"));
    }

    @Test
    void testReadInt() throws Exception {
        provideInput("abc\n10.5\n42\n"); // Invalid, Invalid, Valid
        assertEquals(42, InputValidator.readInt("Prompt"));
    }

    @Test
    void testReadDouble() throws Exception {
        provideInput("abc\n10.5\n");
        assertEquals(10.5, InputValidator.readDouble("Prompt"), 0.001);
    }

    @Test
    void testReadDate() throws Exception {
        provideInput("bad\n2000-01-01 10:00\n2099-12-31 10:00\n"); // Format, Past, Valid
        LocalDateTime dt = InputValidator.readDateTime("Prompt");
        assertEquals(2099, dt.getYear());
    }
    
    @Test
    void testConstructor() {
        assertNotNull(new InputValidator()); // Covers implicit constructor
    }
}