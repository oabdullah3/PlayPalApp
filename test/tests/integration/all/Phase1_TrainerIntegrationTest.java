package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.entities.Trainer;
import app.managers.Database;

/**
 * INTEGRATION PHASE 1: Trainer Constructor & Methods
 * 
 * Tests the Trainer class which depends on:
 * - User constructor (unit tested)
 * - TrainerState (unit tested)
 */
public class Phase1_TrainerIntegrationTest {

    @BeforeEach
    void setUp() {
        Database.getInstance().getAllUsers().clear();
    }

    @Test
    void testTrainerConstructor() {
        Trainer trainer = new Trainer("Coach Mark", "mark@example.com", "password123", "Tennis", 75.0);

        assertNotNull(trainer.getId());
        assertEquals("Coach Mark", trainer.getName());
        assertEquals("mark@example.com", trainer.getEmail());
        assertEquals("password123", trainer.getPassword());
        assertEquals(0.0, trainer.getBalance(), "Trainers start with 0 balance");
        assertEquals("Tennis", trainer.getSpecialty());
        assertEquals(75.0, trainer.getHourlyRate());
        assertFalse(trainer.isApproved(), "New trainers should not be approved by default");
    }

    @Test
    void testTrainerShowMenuOptions() {
        Trainer trainer = new Trainer("Coach Sarah", "sarah@example.com", "pass456", "Basketball", 50.0);
        assertDoesNotThrow(() -> trainer.showMenuOptions());
    }

    @Test
    void testTrainerSpecialtyAndRateSetter() {
        Trainer trainer = new Trainer("Coach Tom", "tom@example.com", "pass789", "Football", 60.0);

        trainer.setSpecialty("Soccer");
        assertEquals("Soccer", trainer.getSpecialty());

        trainer.setHourlyRate(80.0);
        assertEquals(80.0, trainer.getHourlyRate());
    }

    @Test
    void testTrainerApprovalStatus() {
        Trainer trainer = new Trainer("Coach Lisa", "lisa@example.com", "pass000", "Yoga", 40.0);
        
        assertFalse(trainer.isApproved());
        
        trainer.setApproved(true);
        assertTrue(trainer.isApproved());

        trainer.setApproved(false);
        assertFalse(trainer.isApproved());
    }

    @Test
    void testTrainerInheritedMethods() {
        Trainer trainer = new Trainer("Coach John", "john@example.com", "pass111", "Pilates", 55.0);

        trainer.setName("Coach John Updated");
        assertEquals("Coach John Updated", trainer.getName());

        trainer.setBalance(500.0);
        assertEquals(500.0, trainer.getBalance());
    }

    @Test
    void testTrainerExecuteMenu() {
        Trainer trainer = new Trainer("Coach Eve", "eve@example.com", "pass222", "Boxing", 70.0);
        assertDoesNotThrow(() -> trainer.executeMenu());
    }

    @Test
    void testTrainerStartsWithZeroBalance() {
        Trainer trainer1 = new Trainer("Trainer 1", "t1@test.com", "pass", "Tennis", 50.0);
        Trainer trainer2 = new Trainer("Trainer 2", "t2@test.com", "pass", "Soccer", 60.0);
        
        assertEquals(0.0, trainer1.getBalance());
        assertEquals(0.0, trainer2.getBalance());
    }

    @Test
    void testTrainerShowMenuWithApprovedStatus() {
        Trainer trainer = new Trainer("Coach Approved", "approved@example.com", "pass555", "Gym", 65.0);
        
        // Approve the trainer to test the "APPROVED" branch
        trainer.setApproved(true);
        assertTrue(trainer.isApproved());
        
        // Should not throw an exception and should display with APPROVED status
        assertDoesNotThrow(() -> trainer.showMenuOptions());
    }
}