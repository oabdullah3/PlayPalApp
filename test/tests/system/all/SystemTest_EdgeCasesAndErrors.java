package tests.system.all;

import app.main.PlayPalApp;
import app.managers.Database;
import app.entities.Trainer;
import app.entities.Player;
import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest_EdgeCasesAndErrors {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream(); // Capture StdErr

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        Database.getInstance().getAllUsers().clear();
        Database.getInstance().getAllBookings().clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore & Reset
        System.setIn(System.in);
        System.setOut(System.out);
        System.setErr(System.err);
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(System.in));
    }

    private void prepareInput(String data) throws Exception {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(testIn));
    }

    @Test
    void testDuplicateRegistration() throws Exception {
        String input = 
            // 1. Register First User
            "2\nUser1\ndup@test.com\npw\n1\n" +
            // 2. Try Registering Duplicate
            "2\nUser2\ndup@test.com\npw\n1\n" +
            "3\n"; // Exit

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Registration Failed"));
        assertTrue(errorOutput.contains("Email already in use"));
    }

    @Test
    void testLoginFailures() throws Exception {
        String input = 
            // 1. Wrong Email
            "1\nmissing@test.com\npw\n" +
            // 2. Correct Email, Wrong Pass
            // First register valid user to test bad pass
            "2\nValid\nvalid@test.com\npw\n1\n" +
            "1\nvalid@test.com\nWRONG_PASS\n" +
            "3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("User not found"));
        assertTrue(errorOutput.contains("Invalid password"));
    }

    @Test
    void testBookingInsufficientFunds() throws Exception {
        // Pre-condition: Trainer
        Trainer t = new Trainer("Coach", "c@c.com", "pw", "Gym", 100.0);
        t.setApproved(true);
        Database.getInstance().getAllUsers().add(t);
        String tId = t.getId().substring(0, 4);

        String input = 
            // 1. Register Broke Player (Balance $50)
            "2\nBroke\nb@b.com\npw\n1\n" +
            // 2. Login
            "1\nb@b.com\npw\n" +
            // 3. Try to book (Cost $100)
            "3\nGym\n" + tId + "\n1\n" +
            // 4. Logout
            "5\n3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Booking Failed"));
        assertTrue(errorOutput.contains("Required: $100.0"));
    }
}