package tests.unit.all;

import app.entities.Trainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTest {

    @Test
    void testTrainerSpecificFields() {
        // Covers: Trainer constructor, setters, getters
        Trainer t = new Trainer("Coach", "coach@test.com", "pass", "Cardio", 20.0);
        
        t.setSpecialty("Weights");
        t.setHourlyRate(30.0);
        
        // Default state check
        assertFalse(t.isApproved());
        
        // State change check
        t.setApproved(true);

        assertEquals("Weights", t.getSpecialty());
        assertEquals(30.0, t.getHourlyRate(), 0.001);
        assertTrue(t.isApproved());
    }

    @Test
    void testTrainerMenuOptions_PendingBranch() {
        // Covers: Trainer.showMenuOptions -> Ternary Branch "PENDING"
        Trainer pendingTrainer = new Trainer("T1", "t@t.com", "p", "S", 10.0);
        pendingTrainer.setApproved(false);
        
        // Ensure the method runs for unapproved trainers
        assertDoesNotThrow(() -> pendingTrainer.showMenuOptions());
    }

    @Test
    void testTrainerMenuOptions_ApprovedBranch() {
        // Covers: Trainer.showMenuOptions -> Ternary Branch "APPROVED"
        Trainer approvedTrainer = new Trainer("T2", "t2@t.com", "p", "S", 10.0);
        approvedTrainer.setApproved(true);
        
        // Ensure the method runs for approved trainers
        assertDoesNotThrow(() -> approvedTrainer.showMenuOptions());
    }
}