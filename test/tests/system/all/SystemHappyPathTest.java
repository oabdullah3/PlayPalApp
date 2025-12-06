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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemHappyPathTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        AuthManager.getInstance().logout();
        Database.getInstance().getAllMessages().clear(); 
        
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
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
    @Order(1)
    void testRegisterAndCreateSession() {
        String uniqueEmail = "host_" + UUID.randomUUID().toString().substring(0, 5) + "@sys.com";
        String uniqueSport = "SkyDiving_" + UUID.randomUUID().toString().substring(0, 4);
        String futureDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder inputs = new StringBuilder();

        inputs.append("2\n");                // Main Menu: Register

        inputs.append("\n");                 
        inputs.append("Happy User\n");       // Name (Retried after empty error)
        inputs.append(uniqueEmail + "\n");   // Email
        inputs.append("pass123\n");          // Password
        inputs.append("1\n");                // Register as (1) Player

        inputs.append("1\n");                // Main Menu: Login
        inputs.append(uniqueEmail + "\n");   // Email
        inputs.append("pass123\n");          // Password

        inputs.append("1\n");                // Player Menu: Create Session
        inputs.append(uniqueSport + "\n");   // Sport Name
        inputs.append("The Hangar\n");       // Location
        inputs.append(futureDate + "\n");    // Date
        inputs.append("5\n");                // Capacity

        inputs.append("5\n");                // Player Menu: Logout
        inputs.append("3\n");                // Main Menu: Exit

        setInput(inputs.toString());

        try {
            PlayPalApp.main(new String[]{});
        } catch (Exception e) {
            e.printStackTrace(); 
        }

        String consoleOutput = outContent.toString();

        assertTrue(consoleOutput.contains("Registration successful"), 
            "Failed to register user. Output Trace:\n" + consoleOutput);
            
        assertTrue(consoleOutput.contains("Login successful"), 
            "Failed to login.");
            
        assertTrue(consoleOutput.contains("Session created successfully"), 
            "Failed to create session.");
            
        assertTrue(consoleOutput.contains("Thank you for using PlayPal"), 
            "App did not exit gracefully.");
    }
}