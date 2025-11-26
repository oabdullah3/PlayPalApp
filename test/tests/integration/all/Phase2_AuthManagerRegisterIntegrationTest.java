package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Player;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.DuplicateEmailException;

/**
 * INTEGRATION PHASE 2: AuthManager.register()
 * 
 * Tests the register method which depends on:
 * - Database (unit tested)
 * - Player constructor (Phase 1)
 * - Trainer constructor (Phase 1)
 * - Duplicate email checking
 */
public class Phase2_AuthManagerRegisterIntegrationTest {

    private AuthManager authManager;

    @BeforeEach
    void setUp() {
        authManager = AuthManager.getInstance();
        Database.getInstance().getAllUsers().clear();
    }

    @Test
    void testRegisterNewPlayer() throws DuplicateEmailException {
        authManager.register("Alice Player", "alice@test.com", "password123", false, "", 0);
        
        assertEquals(1, Database.getInstance().getAllUsers().size());
        User user = Database.getInstance().getAllUsers().get(0);
        
        assertTrue(user instanceof Player);
        assertEquals("Alice Player", user.getName());
        assertEquals("alice@test.com", user.getEmail());
        assertEquals(50.0, user.getBalance());
    }

    @Test
    void testRegisterNewTrainer() throws DuplicateEmailException {
        authManager.register("Bob Trainer", "bob@test.com", "password456", true, "Tennis", 75.0);
        
        assertEquals(1, Database.getInstance().getAllUsers().size());
        User user = Database.getInstance().getAllUsers().get(0);
        
        assertTrue(user instanceof Trainer);
        assertEquals("Bob Trainer", user.getName());
        assertEquals("bob@test.com", user.getEmail());
        
        Trainer trainer = (Trainer) user;
        assertEquals("Tennis", trainer.getSpecialty());
        assertEquals(75.0, trainer.getHourlyRate());
        assertEquals(0.0, trainer.getBalance());
        assertFalse(trainer.isApproved());
    }

    @Test
    void testRegisterDuplicateEmail() throws DuplicateEmailException {
        authManager.register("User One", "duplicate@test.com", "pass1", false, "", 0);
        
        assertThrows(DuplicateEmailException.class, () ->
            authManager.register("User Two", "duplicate@test.com", "pass2", false, "", 0)
        );
    }

    @Test
    void testRegisterMultipleUsers() throws DuplicateEmailException {
        authManager.register("Player 1", "player1@test.com", "pass1", false, "", 0);
        authManager.register("Trainer 1", "trainer1@test.com", "pass2", true, "Soccer", 60.0);
        authManager.register("Player 2", "player2@test.com", "pass3", false, "", 0);
        
        assertEquals(3, Database.getInstance().getAllUsers().size());
    }

    @Test
    void testRegisterCaseInsensitiveDuplicateCheck() throws DuplicateEmailException {
        authManager.register("User One", "test@test.com", "pass1", false, "", 0);
        
        assertThrows(DuplicateEmailException.class, () ->
            authManager.register("User Two", "TEST@TEST.COM", "pass2", false, "", 0)
        );
    }

    @Test
    void testRegisterTrainerWithEmptySpecialty() throws DuplicateEmailException {
        authManager.register("Trainer", "trainer@test.com", "pass", true, "", 50.0);
        
        User user = Database.getInstance().getAllUsers().get(0);
        assertTrue(user instanceof Trainer);
        assertEquals("", ((Trainer) user).getSpecialty());
    }

    @Test
    void testRegisterPlayerIgnoresSpecialtyAndRate() throws DuplicateEmailException {
        authManager.register("Player", "player@test.com", "pass", false, "Tennis", 75.0);
        
        User user = Database.getInstance().getAllUsers().get(0);
        assertTrue(user instanceof Player);
        assertEquals(50.0, user.getBalance()); // Players always get 50
    }
}
