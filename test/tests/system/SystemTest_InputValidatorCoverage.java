package tests.system;

import app.main.PlayPalApp;
import app.managers.Database;
import app.entities.Trainer;
import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class SystemTest_InputValidatorCoverage {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(System.out);
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(System.in));
    }

    private void prepareInput(String data) throws Exception {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(System.in));
    }

    @Test
    void testInputValidatorMethods() throws Exception {
        // Scenario to force usage of readDouble and readOptionalString
        
        String input = 
            // 1. Register Trainer (Triggers readDouble for rate)
            "2\n" +
            "ValidatorTrainer\n" +
            "val@test.com\n" +
            "pw\n" +
            "2\n" +                 // Type: Trainer
            "Yoga\n" +              // Specialty
            "50.55\n" +             // Rate (Triggers readDouble)
            
            // 2. Login
            "1\nval@test.com\npw\n" +
            
            // 3. Update Profile (Triggers readOptionalString)
            "2\n" +                 // Update Profile
            "\n" +                  // Name: Empty (Skip) -> readOptionalString
            "\n" +                  // Rate: Empty (Skip) -> readOptionalString
            "NewSpec\n" +           // Spec: Change -> readOptionalString
            
            "4\n3\n";               // Logout, Exit

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        
        // Verify readDouble worked
        assertTrue(output.contains("Account created as Trainer"), "Registration with double failed");
        
        // Verify readOptionalString worked (Name kept, Spec changed)
        Trainer t = (Trainer) Database.getInstance().getAllUsers().get(0);
        assertEquals("ValidatorTrainer", t.getName(), "Name should be skipped/kept");
        assertEquals("NewSpec", t.getSpecialty(), "Specialty should be updated");
        assertEquals(50.55, t.getHourlyRate(), 0.01, "Double rate should be stored");
    }
}