package tests.integration.all;

import app.entities.Trainer;
import app.entities.User;
import app.main.PlayPalApp;
import app.managers.AuthManager;
import app.managers.Database;
import app.ui.UserUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayPalAppTest {

    private static PlayPalApp app;
    private static AuthManager authManager;
    private static Database db;

    // Test Data
    private static String playerEmail;
    private static String trainerEmail;
    private static String adminEmail;

    // Output Capture
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeAll
    static void setupGlobal() {
        app = new PlayPalApp();
        authManager = AuthManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        playerEmail = "sys_p_" + suffix + "@app.com";
        trainerEmail = "sys_t_" + suffix + "@app.com";
        
        // Target the existing Admin
        adminEmail = "admin@playpal.com"; 

        try {
            // 1. Setup Player (We still create these for the other tests)
            User p = new app.entities.Player("Sys Player", playerEmail, "pass");
            db.addUser(p);
            
            // 2. Setup Trainer (Approved)
            Trainer t = new app.entities.Trainer("Sys Trainer", trainerEmail, "pass", "Run", 50.0);
            t.setApproved(true);
            db.addUser(t);

            // REMOVED: No longer adding Admin user here. 
            // We assume admin@playpal.com exists in the DB or CSV files already.

        } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() {
        authManager.logout();
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

    // --- 1. Main Method Entry Point ---

    @Test
    @Order(1)
    void testMainMethod_Exit() {
        provideInput("3\n");
        assertDoesNotThrow(() -> PlayPalApp.main(new String[]{}));
        assertTrue(outContent.toString().contains("Welcome to PlayPal CLI"));
        assertTrue(outContent.toString().contains("Goodbye"));
    }

    // --- 2. User Flows ---

    @Test
    @Order(2)
    void testFullSystemFlow_Player() throws Exception {
        String inputSequence = 
            "1\n" +             // Login
            playerEmail + "\n" +
            "pass\n" +
            "5\n" +             // Player Logout
            "3\n";              // Exit App

        provideInput(inputSequence);
        invokeRunMainMenu();

        String output = outContent.toString();
        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("Player Dashboard"));
        assertTrue(output.contains("Logout successful"));
    }

    @Test
    @Order(3)
    void testFullSystemFlow_Trainer() throws Exception {
        String inputSequence = 
            "1\n" +             
            trainerEmail + "\n" +
            "pass\n" +
            "1\n" +             // View Bookings
            "4\n" +             // Logout
            "3\n";              

        provideInput(inputSequence);
        invokeRunMainMenu();

        String output = outContent.toString();
        assertTrue(output.contains("Trainer Dashboard"));
        assertTrue(output.contains("Trainer Bookings"));
    }

    @Test
    @Order(4)
    void testFullSystemFlow_Admin() throws Exception {
        // CORRECTED: Using existing admin credentials
        String inputSequence = 
            "1\n" + 
            adminEmail + "\n" +
            "admin123\n" +      // Updated password to match existing admin
            "2\n" +             // Admin Dashboard: Logout
            "3\n";              // Main Menu: Exit

        provideInput(inputSequence);
        invokeRunMainMenu();

        String output = outContent.toString();
        assertTrue(output.contains("ADMIN DASHBOARD"), "Should correctly route to Admin UI");
        assertTrue(output.contains("System Status Report"));
    }

    @Test
    @Order(5)
    void testFullSystemFlow_Register() throws Exception {
        String newEmail = "new_" + UUID.randomUUID() + "@test.com";
        String inputSequence = 
            "2\n" +             // Register
            "New User\n" + 
            newEmail + "\n" +
            "pass\n" + 
            "1\n" +             // Player
            "3\n";              // Exit

        provideInput(inputSequence);
        invokeRunMainMenu();

        assertTrue(outContent.toString().contains("Registration successful"));
    }

    // --- 3. Edge Cases ---

    @Test
    @Order(6)
    void testInvalidMainMenuOption() throws Exception {
        String inputSequence = "99\n3\n";
        provideInput(inputSequence);
        invokeRunMainMenu();
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    @Order(7)
    void testExceptionInMenuLoop() throws Exception {
        // Simulate sudden input stream cut-off to force error handling
        provideInput("1\n"); 
        
        try {
            invokeRunMainMenu();
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    @Order(8)
    void testDashboard_MissingUI_EdgeCase() throws Exception {
        // 1. Hack the uiDispatcher map to remove Player.class
        Field mapField = PlayPalApp.class.getDeclaredField("uiDispatcher");
        mapField.setAccessible(true);
        Map<Class<?>, UserUI> dispatcher = (Map<Class<?>, UserUI>) mapField.get(app);
        dispatcher.remove(app.entities.Player.class); 

        // 2. Prepare User & Input
        User player = authManager.login(playerEmail, "pass");
        provideInput("1\n"); 

        // 3. Invoke runDashboard directly
        Method runDash = PlayPalApp.class.getDeclaredMethod("runDashboard", User.class);
        runDash.setAccessible(true);
        
        try {
            runDash.invoke(app, player);
        } catch (Exception e) {}

        // 4. Assert Error
        assertTrue(outContent.toString().contains("Error: No UI dashboard found"));
            
        // Restore map
        dispatcher.put(app.entities.Player.class, new app.ui.PlayerUI());
    }

    private void invokeRunMainMenu() throws Exception {
        Method method = PlayPalApp.class.getDeclaredMethod("runMainMenu");
        method.setAccessible(true);
        method.invoke(app);
    }
}