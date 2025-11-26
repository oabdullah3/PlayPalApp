package tests.integration.phase1;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.entities.Player;
import app.entities.Trainer;
import app.managers.Database;

/**
 * INTEGRATION PHASE 1: User.executeMenu()
 * 
 * Tests the executeMenu() method with Player and Trainer implementations
 * Dependencies: Player (Phase 1), Trainer (Phase 1), PlayerState (unit tested), TrainerState (unit tested)
 */
public class Phase1_UserExecuteMenuIntegrationTest {

    @BeforeEach
    void setUp() {
        Database.getInstance().getAllUsers().clear();
    }

    @Test
    void testPlayerExecuteMenu() {
        Player player = new Player("Test Player", "test@example.com", "password");
        assertDoesNotThrow(() -> player.executeMenu());
    }

    @Test
    void testTrainerExecuteMenu() {
        Trainer trainer = new Trainer("Test Trainer", "trainer@example.com", "password", "Tennis", 75.0);
        assertDoesNotThrow(() -> trainer.executeMenu());
    }

    @Test
    void testExecuteMenuMultipleTimes() {
        Player player = new Player("Alice", "alice@test.com", "pass");
        
        assertDoesNotThrow(() -> {
            player.executeMenu();
            player.executeMenu();
            player.executeMenu();
        });
    }
}
