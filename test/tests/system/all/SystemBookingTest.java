package tests.system.all;

import app.main.PlayPalApp;
import app.entities.Player;
import app.entities.Trainer;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SystemManager;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemBookingTest {

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
    void testTrainerBookingFlow_SingleRun() {
        
        String trainerEmail = "coach_" + UUID.randomUUID().toString().substring(0,5) + "@sys.com";
        String playerEmail = "rich_" + UUID.randomUUID().toString().substring(0,5) + "@sys.com";
        String specialty = "SystemYoga_" + UUID.randomUUID().toString().substring(0,4);

        Trainer trainer = new Trainer("Coach Carter", trainerEmail, "pass123", specialty, 50.0);
        Database.getInstance().addUser(trainer);
        SystemManager.getInstance().approveTrainer(trainer.getId()); // Approve
        
        Player player = new Player("Rich Client", playerEmail, "pass123");
        player.setBalance(500.0);
        Database.getInstance().addUser(player); 

        String trainerIdPrefix = trainer.getId().substring(0, 6);

        StringBuilder inputs = new StringBuilder();

        inputs.append("1\n");                  // Main: Login
        inputs.append(playerEmail + "\n");     // Email
        inputs.append("pass123\n");            // Pass

        inputs.append("3\n");                  // Player Menu: Book
        inputs.append(specialty + "\n");       // Search Specialty
        inputs.append(trainerIdPrefix + "\n"); // Enter Trainer ID
        inputs.append("2\n");                  // Duration (2 hours = $100, Balance is $500)

        inputs.append("5\n");                  // Logout
        inputs.append("3\n");                  // Exit

        setInput(inputs.toString());
        PlayPalApp.main(null);

        String output = outContent.toString();
        
        assertTrue(output.contains("Booking successful") || output.contains("confirmed"),
            "Booking failed. Full Console Output:\n" + output);
    }
}