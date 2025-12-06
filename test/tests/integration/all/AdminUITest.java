package tests.integration.all;

import app.entities.Trainer;
import app.entities.User;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SystemManager;
import app.ui.AdminUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminUITest {

    private static AdminUI adminUI;
    private static AuthManager authManager;
    private static Database db;
    private static SystemManager systemManager;
    private static String trainerId;
    private static String adminEmail;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void setupGlobal() {
        adminUI = new AdminUI();
        authManager = AuthManager.getInstance();
        db = Database.getInstance();
        systemManager = SystemManager.getInstance();
        
        String suffix = UUID.randomUUID().toString().substring(0, 5);
        adminEmail = "admin_" + suffix + "@playpal.com";

        try {
            authManager.register("Pending T", "pt_" + suffix + "@t.com", "pass", true, "Gym", 20);
            trainerId = authManager.login("pt_" + suffix + "@t.com", "pass").getId();
            authManager.logout();
            
            authManager.register("Admin User", adminEmail, "pass", false, null, 0);
        } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() {
        authManager.logout();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
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
    void testRunAdminDashboard_InvalidChoice() throws Exception {
        authManager.login(adminEmail, "pass");

        String inputs = "99\n2\n";
        provideInput(inputs);

        adminUI.runAdminDashboard();

        assertTrue(outContent.toString().contains("Invalid choice"), "Should hit default switch case");
    }

    @Test
    @Order(2)
    void testRunAdminDashboard_ApproveTrainer() throws Exception {
        authManager.login(adminEmail, "pass");

        String inputs = "1\n" + trainerId.substring(0, 6) + "\n2\n";
        provideInput(inputs);

        adminUI.runAdminDashboard();

        assertTrue(outContent.toString().contains("approved successfully"));
        
        User t = db.findUserByIdPrefix(trainerId);
        assertTrue(((Trainer) t).isApproved());
    }

    @Test
    @Order(3)
    void testHandleTrainerApproval_NoPending() throws Exception {
        List<Trainer> pending = db.findPendingTrainers();
        for (Trainer t : pending) {
            systemManager.approveTrainer(t.getId());
        }

        authManager.login(adminEmail, "pass");

        String inputs = "1\n2\n";
        provideInput(inputs);

        adminUI.runAdminDashboard();

        assertTrue(outContent.toString().contains("No pending trainer requests"), "Should hit isEmpty block");
    }
}