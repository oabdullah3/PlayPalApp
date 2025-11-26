package tests.integration.phase3;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.SystemManager;
import app.managers.Database;
import app.entities.Trainer;
import app.entities.Player;

/**
 * INTEGRATION PHASE 3: SystemManager.approveTrainer()
 * 
 * Tests the approveTrainer method which depends on:
 * - Database (unit tested)
 * - Trainer entity (Phase 1)
 * - Trainer.setApproved() (unit tested)
 */
public class Phase3_SystemManagerApprovTrainerIntegrationTest {

    private SystemManager systemManager;

    @BeforeEach
    void setUp() {
        systemManager = SystemManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        
        // Add test trainers
        Trainer trainer1 = new Trainer("Coach Alice", "alice@test.com", "pass", "Tennis", 50.0);
        Trainer trainer2 = new Trainer("Coach Bob", "bob@test.com", "pass", "Soccer", 60.0);
        Player player = new Player("John Player", "john@test.com", "pass");
        
        db.getAllUsers().add(trainer1);
        db.getAllUsers().add(trainer2);
        db.getAllUsers().add(player);
    }

    @Test
    void testApproveTrainerWithValidId() {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(0);
        String trainerId = trainer.getId().substring(0, 8);
        
        assertFalse(trainer.isApproved());
        
        systemManager.approveTrainer(trainerId);
        
        assertTrue(trainer.isApproved());
    }

    @Test
    void testApproveTrainerWithFullId() {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(0);
        
        assertFalse(trainer.isApproved());
        
        systemManager.approveTrainer(trainer.getId());
        
        assertTrue(trainer.isApproved());
    }

    @Test
    void testApproveMultipleTrainers() {
        Database db = Database.getInstance();
        Trainer trainer1 = (Trainer) db.getAllUsers().get(0);
        Trainer trainer2 = (Trainer) db.getAllUsers().get(1);
        
        systemManager.approveTrainer(trainer1.getId().substring(0, 8));
        systemManager.approveTrainer(trainer2.getId().substring(0, 8));
        
        assertTrue(trainer1.isApproved());
        assertTrue(trainer2.isApproved());
    }

    @Test
    void testApproveTrainerWithNonexistentId() {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(0);
        
        assertFalse(trainer.isApproved());
        
        // Should not throw, just print message
        assertDoesNotThrow(() -> systemManager.approveTrainer("nonexistent"));
        
        // Original trainer should still be unapproved
        assertFalse(trainer.isApproved());
    }

    @Test
    void testApprovePlayerShouldNotAffect() {
        Database db = Database.getInstance();
        Player player = (Player) db.getAllUsers().get(2);
        
        // Trying to approve a player should not cause issues
        assertDoesNotThrow(() -> systemManager.approveTrainer(player.getId().substring(0, 8)));
    }

    @Test
    void testApproveTrainerAlreadyApproved() {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(0);
        
        systemManager.approveTrainer(trainer.getId().substring(0, 8));
        assertTrue(trainer.isApproved());
        
        // Approve again
        systemManager.approveTrainer(trainer.getId().substring(0, 8));
        assertTrue(trainer.isApproved());
    }
}
