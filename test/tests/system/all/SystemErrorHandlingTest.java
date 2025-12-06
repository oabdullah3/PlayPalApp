package tests.system.all;

import app.main.PlayPalApp;
import app.managers.AuthManager;
import app.managers.Database;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemErrorHandlingTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream(); // Capture ERR
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        // Capture both streams
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent)); 
        
        AuthManager.getInstance().logout();
        Database.getInstance().getAllMessages().clear(); 
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        try {
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, new Scanner(System.in));
        } catch (Exception e) {
            fail("Failed to reset scanner: " + e.getMessage());
        }
    }

    @Test
    void testInvalidScenarios() {
        String existingEmail = "duplicate_" + UUID.randomUUID() + "@test.com";
        
        try {
            AuthManager.getInstance().register("Original", existingEmail, "pass", false, null, 0);
        } catch (Exception e) {}

        StringBuilder input = new StringBuilder();

        input.append("1\n");                // Main: Login
        input.append("\n");                 // Handle leftover newline
        input.append(existingEmail + "\n"); // Valid Email
        input.append("WRONG_PASS\n");       // Invalid Password

        input.append("1\n");                // Main: Login
        input.append("\n");                 // Handle leftover newline
        input.append("ghost@ghost.com\n");  // Non-existent Email
        input.append("pass\n");

        input.append("2\n");                // Main: Register
        input.append("\n");                 // Handle leftover newline
        input.append("Copy Cat\n");         // Name
        input.append(existingEmail + "\n"); // Duplicate Email
        input.append("newpass\n");          // Password
        input.append("1\n");                // Type: Player

        input.append("1\n");                // Main: Login
        input.append("\n");                 // Handle leftover newline
        input.append(existingEmail + "\n");
        input.append("pass\n");

        input.append("2\n");                // Player: Join Session
        input.append("\n");                 // Handle leftover newline
        input.append("Soccer\n");           // Search Sport
        input.append("NON_EXISTENT_ID\n"); 

        input.append("5\n");                // Player: Logout
        input.append("3\n");                // Main: Exit

        setInput(input.toString());
        
        try { 
            PlayPalApp.main(null); 
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fullLog = outContent.toString() + errContent.toString();

        assertTrue(fullLog.contains("Invalid password") || fullLog.contains("failed"), 
            "Should detect invalid password.\nFull Log:\n" + fullLog);
            
        assertTrue(fullLog.contains("User not found") || fullLog.contains("failed"), 
            "Should detect non-existent user.\nFull Log:\n" + fullLog);
            
        assertTrue(fullLog.contains("Email already in use") || fullLog.contains("Registration failed"), 
            "Should prevent duplicate registration.\nFull Log:\n" + fullLog);
    }
}