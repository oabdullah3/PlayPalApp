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

    private void provideInput(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
            Scanner mockScanner = new Scanner(inputStream);

            Field scannerField = InputValidator.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            
            scannerField.set(null, mockScanner);
            
        } catch (Exception e) {
            fail("Failed to inject mock scanner: " + e.getMessage());
        }
    }

    @Test
    void testReadOptionalString() {
        provideInput("  Hello World  \n");

        String result = InputValidator.readOptionalString("Enter optional: ");
        
        assertEquals("Hello World", result);
    }

    @Test
    void testReadString_ValidationLoop() {
        String inputSequence = "\nValid Name\n"; 
        provideInput(inputSequence);

        String result = InputValidator.readString("Enter name: ");

        assertEquals("Valid Name", result);
    }

    @Test
    void testReadInt_ExceptionHandling() {
        String inputSequence = "abc\n42\n";
        provideInput(inputSequence);

        int result = InputValidator.readInt("Enter number: ");

        assertEquals(42, result);
    }

    @Test
    void testReadDouble_ExceptionHandling() {
        String inputSequence = "xyz\n99.99\n";
        provideInput(inputSequence);

        double result = InputValidator.readDouble("Enter price: ");

        assertEquals(99.99, result, 0.001);
    }

    @Test
    void testReadDateTime_FullCoverage() {
        String inputSequence = 
            "bad-format\n" +            
            "2000-01-01 10:00\n" +      
            "2099-12-31 12:00\n";       
        
        provideInput(inputSequence);

        LocalDateTime result = InputValidator.readDateTime("Enter time");

        assertEquals(2099, result.getYear());
        assertEquals(12, result.getMonthValue());
    }
    
    @Test
    void testConstructor() {
        new InputValidator(); 
    }
}