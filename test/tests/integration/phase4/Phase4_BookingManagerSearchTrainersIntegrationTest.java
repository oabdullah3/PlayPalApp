package tests.integration.phase4;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.BookingManager;
import app.managers.Database;
import app.entities.Trainer;
import java.util.List;

/**
 * INTEGRATION PHASE 4: BookingManager.searchApprovedTrainers()
 * 
 * Tests the searchApprovedTrainers method which depends on:
 * - Database (unit tested)
 * - Trainer entity (Phase 1)
 * - Trainer.isApproved() and getSpecialty() (unit tested)
 */
public class Phase4_BookingManagerSearchTrainersIntegrationTest {

    private BookingManager bookingManager;

    @BeforeEach
    void setUp() {
        bookingManager = BookingManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        
        // Create trainers with different statuses and specialties
        Trainer approved1 = new Trainer("Coach Alice", "alice@test.com", "pass", "Tennis", 50.0);
        approved1.setApproved(true);
        
        Trainer approved2 = new Trainer("Coach Bob", "bob@test.com", "pass", "Tennis", 60.0);
        approved2.setApproved(true);
        
        Trainer approved3 = new Trainer("Coach Carol", "carol@test.com", "pass", "Soccer", 70.0);
        approved3.setApproved(true);
        
        Trainer notApproved = new Trainer("Coach Dave", "dave@test.com", "pass", "Tennis", 80.0);
        // notApproved.setApproved(false); // Default is false
        
        db.getAllUsers().add(approved1);
        db.getAllUsers().add(approved2);
        db.getAllUsers().add(approved3);
        db.getAllUsers().add(notApproved);
    }

    @Test
    void testSearchApprovedTrainersBySpecialty() {
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Tennis");
        
        assertEquals(2, trainers.size());
        assertTrue(trainers.stream().allMatch(t -> t.getSpecialty().equalsIgnoreCase("Tennis")));
        assertTrue(trainers.stream().allMatch(Trainer::isApproved));
    }

    @Test
    void testSearchApprovedTrainersExcludesUnapproved() {
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Tennis");
        
        // Should have 2 approved Tennis trainers, not 3
        assertEquals(2, trainers.size());
        
        // Verify none are Dave (the unapproved one)
        assertTrue(trainers.stream().noneMatch(t -> t.getName().equals("Coach Dave")));
    }

    @Test
    void testSearchApprovedTrainersDifferentSpecialty() {
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Soccer");
        
        assertEquals(1, trainers.size());
        assertEquals("Coach Carol", trainers.get(0).getName());
        assertTrue(trainers.get(0).isApproved());
    }

    @Test
    void testSearchApprovedTrainersNoResults() {
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Karate");
        
        assertTrue(trainers.isEmpty());
    }

    @Test
    void testSearchApprovedTrainersCaseInsensitive() {
        List<Trainer> trainers1 = bookingManager.searchApprovedTrainers("tennis");
        List<Trainer> trainers2 = bookingManager.searchApprovedTrainers("TENNIS");
        List<Trainer> trainers3 = bookingManager.searchApprovedTrainers("Tennis");
        
        assertEquals(trainers1.size(), trainers2.size());
        assertEquals(trainers2.size(), trainers3.size());
        assertEquals(2, trainers1.size());
    }

    @Test
    void testSearchApprovedTrainersEmptyDatabase() {
        Database.getInstance().getAllUsers().clear();
        
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Tennis");
        
        assertTrue(trainers.isEmpty());
    }
}
