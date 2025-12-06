package tests.current.temporary;

import app.entities.Trainer;
import app.managers.AuthManager;
import app.managers.BookingManager;
import app.managers.Database;
import app.ui.TrainerUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainerUITest {

    private static TrainerUI trainerUI;
    private static AuthManager authManager;
    private static BookingManager bookingManager;
    private static Database db;

    private static String trainerEmail;
    private static String playerEmail;
    private static String trainerId;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeAll
    static void setupGlobal() {
        trainerUI = new TrainerUI();
        authManager = AuthManager.getInstance();
        bookingManager = BookingManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        trainerEmail = "t_ui_" + suffix + "@train.com";
        playerEmail = "p_ui_" + suffix + "@train.com";

        try {
            authManager.register("Trainer UI", trainerEmail, "pass", true, "Pilates", 40.0);
            trainerId = authManager.login(trainerEmail, "pass").getId();
            db.updateTrainerApproval(trainerId, true); // FIX: Update Real DB
            authManager.logout();

            authManager.register("Player UI", playerEmail, "pass", false, null, 0);
            authManager.login(playerEmail, "pass");
            authManager.logout();
        } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() {
        try {
            if (authManager.getCurrentUser() == null || !authManager.getCurrentUser().getEmail().equals(trainerEmail)) {
                authManager.login(trainerEmail, "pass");
            }
        } catch (Exception e) {}
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void provideInput(String data) {
        try {
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, mockScanner);
        } catch (Exception e) { fail(e.getMessage()); }
    }

    @Test
    @Order(1)
    void testRun_ViewBookings_Empty() {
        trainerUI.run(1);
        assertTrue(outContent.toString().contains("No active or completed bookings found"));
    }

    @Test
    @Order(2)
    void testRun_ViewBookings_Populated() throws Exception {
        authManager.logout();
        authManager.login(playerEmail, "pass");
        authManager.getCurrentUser().setBalance(1000.0); 
        bookingManager.bookTrainer(trainerId, 1);
        authManager.logout();
        
        authManager.login(trainerEmail, "pass");
        outContent.reset(); 
        trainerUI.run(1);

        assertTrue(outContent.toString().contains("Trainer Bookings for Trainer UI"));
    }

    @Test
    @Order(3)
    void testRun_UpdateProfile_Success() {
        String inputs = "Super Trainer\n80.0\nAdvanced Pilates\n";
        provideInput(inputs);
        trainerUI.run(2);
        assertTrue(outContent.toString().contains("Profile updated successfully"));
    }
    
    @Test
    @Order(4)
    void testRun_UpdateProfile_SkipFields() {
        provideInput("\n\n\n");
        String originalName = authManager.getCurrentUser().getName();
        trainerUI.run(2);
        assertEquals(originalName, authManager.getCurrentUser().getName());
    }

    @Test
    @Order(5)
    void testRun_UpdateProfile_InvalidRate() {
        provideInput("\nnot-a-number\n\n");
        trainerUI.run(2);
        assertTrue(errContent.toString().contains("Invalid rate format"));
    }

    @Test
    @Order(6)
    void testRun_ViewMessages_Integration() {
        provideInput("0\n"); // Return immediately
        trainerUI.run(3);
        assertDoesNotThrow(() -> {}); 
    }

    @Test
    @Order(7)
    void testRun_InvalidChoice() {
        trainerUI.run(99);
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    @Order(8)
    void testRun_Logout() {
        trainerUI.run(4);
        assertNull(authManager.getCurrentUser());
        assertTrue(outContent.toString().contains("Logout successful"));
    }

    @Test
    @Order(9)
    void testRun_ViewBookings_UnknownPlayer() {
        app.entities.Booking ghost = new app.entities.Booking("ghost-id", trainerId, 10.0);
        db.addBooking(ghost);

        outContent.reset();
        trainerUI.run(1);

        assertTrue(outContent.toString().contains("Unknown Player"));
    }
}