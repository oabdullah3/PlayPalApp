package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Player;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.UserNotFoundException;
import app.exceptions.InvalidCredentialsException;

/**
 * INTEGRATION PHASE 2: AuthManager.login()
 * 
 * Tests the login method which depends on:
 * - Database (unit tested)
 * - User entities and credentials
 * - AuthManager.getCurrentUser() and setters (unit tested)
 */
public class Phase2_AuthManagerLoginIntegrationTest {

    private AuthManager authManager;

    @BeforeEach
    void setUp() {
        authManager = AuthManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        authManager.logout(); // Ensure no user is logged in
        
        // Add test users
        Player player = new Player("John Player", "john@test.com", "password123");
        Trainer trainer = new Trainer("Jane Trainer", "jane@test.com", "password456", "Tennis", 50.0);
        db.getAllUsers().add(player);
        db.getAllUsers().add(trainer);
    }

    @Test
    void testLoginSuccessWithPlayer() throws UserNotFoundException, InvalidCredentialsException {
        User user = authManager.login("john@test.com", "password123");
        
        assertNotNull(user);
        assertEquals("John Player", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertSame(user, authManager.getCurrentUser());
    }

    @Test
    void testLoginSuccessWithTrainer() throws UserNotFoundException, InvalidCredentialsException {
        User user = authManager.login("jane@test.com", "password456");
        
        assertNotNull(user);
        assertEquals("Jane Trainer", user.getName());
        assertTrue(user instanceof Trainer);
        assertSame(user, authManager.getCurrentUser());
    }

    @Test
    void testLoginUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> 
            authManager.login("nonexistent@test.com", "password123")
        );
    }

    @Test
    void testLoginInvalidPassword() {
        assertThrows(InvalidCredentialsException.class, () -> 
            authManager.login("john@test.com", "wrongpassword")
        );
    }

    @Test
    void testLoginCaseInsensitiveEmail() throws UserNotFoundException, InvalidCredentialsException {
        User user = authManager.login("JOHN@TEST.COM", "password123");
        
        assertNotNull(user);
        assertEquals("John Player", user.getName());
    }

    @Test
    void testLoginSetsCurrentUser() throws UserNotFoundException, InvalidCredentialsException {
        assertNull(authManager.getCurrentUser());
        
        User user = authManager.login("john@test.com", "password123");
        
        assertNotNull(authManager.getCurrentUser());
        assertEquals(user.getId(), authManager.getCurrentUser().getId());
    }

    @Test
    void testLoginMultipleTimes() throws UserNotFoundException, InvalidCredentialsException {
        authManager.login("john@test.com", "password123");
        User johnUser = authManager.getCurrentUser();
        
        authManager.login("jane@test.com", "password456");
        User janeUser = authManager.getCurrentUser();
        
        assertNotEquals(johnUser.getId(), janeUser.getId());
    }
}
