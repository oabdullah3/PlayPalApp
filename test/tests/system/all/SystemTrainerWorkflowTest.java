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
class SystemTrainerWorkflowTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(new ByteArrayOutputStream())); 
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
    void testTrainerProfileUpdate() {
        String trainerEmail = "t_self_" + UUID.randomUUID() + "@gym.com";
        
        StringBuilder input = new StringBuilder();

        input.append("2\n");                    // Main: Register
        input.append("\n");                     // Consume leftover newline
        input.append("Self Improver\n");        // Name
        input.append(trainerEmail + "\n");      // Email
        input.append("pass123\n");              // Password
        input.append("2\n");                    // Type: Trainer
        input.append("Basic Yoga\n");           // Specialty
        input.append("40.0\n");                 // Rate

        input.append("1\n");                    // Main: Login
        input.append("\n");                     // Consume leftover newline
        input.append(trainerEmail + "\n");
        input.append("pass123\n");

        input.append("2\n");                    // Trainer Menu: Edit Profile
        input.append("\n");                     // Consume newline
        
       
        input.append("\n");                     // Skip Name (Empty Input)
        input.append("Master Yoga\n");          // New Specialty
        input.append("80.0\n");                 // New Rate

        input.append("1\n");                    // Trainer Menu: View Bookings

        input.append("4\n");                    // Trainer Menu: Logout (Assuming index 4)
        input.append("3\n");                    // Main: Exit

        setInput(input.toString());
        
        try {
            PlayPalApp.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String output = outContent.toString();

        assertTrue(output.contains("Trainer Dashboard") || output.contains("Welcome"), 
            "Should reach Trainer Dashboard. Output:\n" + output);

        assertTrue(output.contains("Profile updated") || output.contains("success"), 
            "Should confirm profile update. Output:\n" + output);
            
        assertTrue(output.contains("No active or completed bookings") || output.contains("Bookings"), 
            "Should display booking status. Output:\n" + output);
    }
}