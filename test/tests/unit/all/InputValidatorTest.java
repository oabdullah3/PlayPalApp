package tests.unit.all;

import app.utils.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    /**
     * Helper method to inject input into the static private Scanner field using Reflection.
     * This simulates user typing into the console.
     */
    private void provideInput(String data) {
        try {
            // Create a scanner that reads from our String data
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
            Scanner mockScanner = new Scanner(inputStream);

            // Use Reflection to access the private static 'scanner' field in InputValidator
            Field scannerField = InputValidator.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            
            // Set the static field to our mock scanner
            // (first argument is null because the field is static)
            scannerField.set(null, mockScanner);
            
        } catch (Exception e) {
            fail("Failed to inject mock scanner: " + e.getMessage());
        }
    }

    @Test
    void testReadOptionalString() {
        // Input: User types "  Hello World  " (should trim)
        provideInput("  Hello World  \n");

        String result = InputValidator.readOptionalString("Enter optional: ");
        
        assertEquals("Hello World", result);
    }

    @Test
    void testReadString_ValidationLoop() {
        // Logic Covered:
        // 1. First input is "" -> Branch: if (input.isEmpty()) is TRUE (Error printed)
        // 2. Second input is "Valid" -> Branch: if (input.isEmpty()) is FALSE (Returns)
        String inputSequence = "\nValid Name\n"; 
        provideInput(inputSequence);

        String result = InputValidator.readString("Enter name: ");

        assertEquals("Valid Name", result);
    }

    @Test
    void testReadInt_ExceptionHandling() {
        // Logic Covered:
        // 1. Input "abc" -> Throws NumberFormatException (Catch block executed)
        // 2. Input "42" -> Valid Integer (Returns)
        String inputSequence = "abc\n42\n";
        provideInput(inputSequence);

        int result = InputValidator.readInt("Enter number: ");

        assertEquals(42, result);
    }

    @Test
    void testReadDouble_ExceptionHandling() {
        // Logic Covered:
        // 1. Input "xyz" -> Throws NumberFormatException (Catch block executed)
        // 2. Input "99.99" -> Valid Double (Returns)
        String inputSequence = "xyz\n99.99\n";
        provideInput(inputSequence);

        double result = InputValidator.readDouble("Enter price: ");

        assertEquals(99.99, result, 0.001);
    }

    @Test
    void testReadDateTime_FullCoverage() {
        // Logic Covered:
        // 1. "bad-format" -> DateTimeParseException (Catch block executed)
        // 2. "2000-01-01 10:00" -> Valid Format but Past -> isBefore(now) is TRUE (Loop continues)
        // 3. "2099-12-31 12:00" -> Valid Format and Future -> Returns
        
        String inputSequence = 
            "bad-format\n" +            // Branch 1: Parse Exception
            "2000-01-01 10:00\n" +      // Branch 2: Past date logic
            "2099-12-31 12:00\n";       // Branch 3: Success
        
        provideInput(inputSequence);

        LocalDateTime result = InputValidator.readDateTime("Enter time");

        assertEquals(2099, result.getYear());
        assertEquals(12, result.getMonthValue());
    }
    
    @Test
    void testConstructor() {
        // Calls the implicit constructor to satisfy 100% coverage
        new InputValidator(); 
    }
}