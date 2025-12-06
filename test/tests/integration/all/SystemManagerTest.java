package tests.integration.all;

import app.entities.Trainer;
import app.entities.User;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SystemManager;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemManagerTest {

    private static SystemManager systemManager;
    private static AuthManager authManager;
    private static String trainerId;
    private static String playerId;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void setupGlobal() {
        systemManager = SystemManager.getInstance();
        authManager = AuthManager.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        try {
            authManager.register("Sys Trainer", "st_" + suffix + "@sys.com", "pass", true, "Gym", 50.0);
            trainerId = authManager.login("st_" + suffix + "@sys.com", "pass").getId();
            authManager.logout();

            authManager.register("Sys Player", "sp_" + suffix + "@sys.com", "pass", false, null, 0);
            playerId = authManager.login("sp_" + suffix + "@sys.com", "pass").getId();
            authManager.logout();
        } catch (Exception e) {}
    }

    @BeforeEach
    void setupStream() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStream() {
        System.setOut(originalOut);
    }


    @Test
    @Order(1)
    void testGetPendingTrainers() {
        List<Trainer> pending = systemManager.getPendingTrainers();
        boolean found = pending.stream().anyMatch(t -> t.getId().equals(trainerId));
        assertTrue(found, "Newly registered trainer should be pending");
    }

    @Test
    @Order(2)
    void testApproveTrainer_Failure_NotFound() {
        systemManager.approveTrainer("garbage_id");
        assertTrue(outContent.toString().contains("Trainer ID not found"));
    }

    @Test
    @Order(3)
    void testApproveTrainer_Failure_NotATrainer() {
        systemManager.approveTrainer(playerId);
        assertTrue(outContent.toString().contains("not a Trainer"));
    }

    @Test
    @Order(4)
    void testApproveTrainer_Success() {
        systemManager.approveTrainer(trainerId);
        
        assertTrue(outContent.toString().contains("approved successfully"));
        
        User t = authManager.getUserById(trainerId);
        assertTrue(((Trainer) t).isApproved());
    }

    @Test
    @Order(5)
    void testDisplaySystemStatus() {
        String report = systemManager.displaySystemStatus();
        assertNotNull(report);
        assertTrue(report.contains("Total Users:"));
        assertTrue(report.contains("Total Sessions:"));
    }
}