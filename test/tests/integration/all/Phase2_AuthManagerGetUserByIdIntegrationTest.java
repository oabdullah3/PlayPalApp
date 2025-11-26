package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Player;
import app.entities.Trainer;
import app.entities.User;

/**
 * INTEGRATION PHASE 2: AuthManager.getUserById()
 * 
 * Tests the getUserById method which depends on:
 * - Database (unit tested)
 * - User entities (already present in database)
 * - Player/Trainer (Phase 1 integration tested)
 */
public class Phase2_AuthManagerGetUserByIdIntegrationTest {

    private AuthManager authManager;

    @BeforeEach
    void setUp() {
        authManager = AuthManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        
        // Add test users
        Player player = new Player("Test Player", "player@test.com", "pass");
        Trainer trainer = new Trainer("Test Trainer", "trainer@test.com", "pass", "Tennis", 50.0);
        db.getAllUsers().add(player);
        db.getAllUsers().add(trainer);
    }

    @Test
    void testGetUserByIdWithPlayerPrefix() {
        Database db = Database.getInstance();
        Player player = (Player) db.getAllUsers().get(0);
        String userId = player.getId().substring(0, 8); // Get prefix

        User foundUser = authManager.getUserById(userId);
        assertNotNull(foundUser);
        assertEquals(player.getId(), foundUser.getId());
    }

    @Test
    void testGetUserByIdWithTrainerPrefix() {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        String userId = trainer.getId().substring(0, 8); // Get prefix

        User foundUser = authManager.getUserById(userId);
        assertNotNull(foundUser);
        assertEquals(trainer.getId(), foundUser.getId());
    }

    @Test
    void testGetUserByIdNotFound() {
        User foundUser = authManager.getUserById("nonexistent");
        assertNull(foundUser);
    }

    @Test
    void testGetUserByIdEmptyDatabase() {
        Database.getInstance().getAllUsers().clear();
        User foundUser = authManager.getUserById("anyprefix");
        assertNull(foundUser);
    }

    @Test
    void testGetUserByIdWithFullId() {
        Database db = Database.getInstance();
        Player player = (Player) db.getAllUsers().get(0);
        
        User foundUser = authManager.getUserById(player.getId());
        assertNotNull(foundUser);
        assertEquals(player.getId(), foundUser.getId());
    }
}
