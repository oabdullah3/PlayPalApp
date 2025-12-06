package tests.integration.all;

import app.entities.Player;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.DuplicateEmailException;
import app.exceptions.InvalidCredentialsException;
import app.exceptions.UserNotFoundException;
import app.managers.AuthManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthManagerTest {

    private AuthManager authManager;

    @BeforeEach
    void setUp() {
        authManager = AuthManager.getInstance();
        authManager.logout();
    }

    private String generateUniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@auth-test.com";
    }


    @Test
    void testRegisterPlayer_Success() {
        String email = generateUniqueEmail();
        
        assertDoesNotThrow(() -> 
            authManager.register("John Player", email, "pass123", false, null, 0.0)
        );

        assertDoesNotThrow(() -> {
            User u = authManager.login(email, "pass123");
            assertTrue(u instanceof Player);
            assertEquals("John Player", u.getName());
        });
    }

    @Test
    void testRegisterTrainer_Success() {
        String email = generateUniqueEmail();

        assertDoesNotThrow(() -> 
            authManager.register("Coach Mike", email, "pass123", true, "Yoga", 50.0)
        );

        assertDoesNotThrow(() -> {
            User u = authManager.login(email, "pass123");
            assertTrue(u instanceof Trainer);
            assertEquals("Yoga", ((Trainer) u).getSpecialty());
        });
    }

    @Test
    void testRegister_DuplicateEmailException() throws DuplicateEmailException {
        String email = generateUniqueEmail();

        authManager.register("User 1", email, "pass", false, null, 0);

        Exception exception = assertThrows(DuplicateEmailException.class, () -> {
            authManager.register("User 2", email, "pass", false, null, 0);
        });

        assertTrue(exception.getMessage().contains("already in use"));
    }

    @Test
    void testLogin_UserNotFoundException() {
        String nonExistentEmail = generateUniqueEmail();

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            authManager.login(nonExistentEmail, "anyPass");
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testLogin_InvalidCredentialsException() throws DuplicateEmailException {
        String email = generateUniqueEmail();
        authManager.register("Valid User", email, "correctPass", false, null, 0);

        Exception exception = assertThrows(InvalidCredentialsException.class, () -> {
            authManager.login(email, "WRONG_PASS");
        });

        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLogin_SetsCurrentUser() throws Exception {
        String email = generateUniqueEmail();
        authManager.register("Login User", email, "pass", false, null, 0);

        authManager.logout();
        assertNull(authManager.getCurrentUser());

        User loggedIn = authManager.login(email, "pass");

        assertNotNull(loggedIn);
        assertEquals(email, authManager.getCurrentUser().getEmail());
        assertEquals(loggedIn, authManager.getCurrentUser());
    }


    @Test
    void testLogout() throws Exception {
        String email = generateUniqueEmail();
        authManager.register("Logout Test", email, "pass", false, null, 0);
        authManager.login(email, "pass");
        
        assertNotNull(authManager.getCurrentUser());

        authManager.logout();

        assertNull(authManager.getCurrentUser());
    }

    @Test
    void testIsAdmin() {
        assertFalse(authManager.isAdmin(null));

        Player normal = new Player("Norm", "norm@test.com", "pass");
        assertFalse(authManager.isAdmin(normal));

        Player admin = new Player("Admin", "admin@playpal.com", "pass");
        assertTrue(authManager.isAdmin(admin));
        
        Player adminCaps = new Player("Admin", "ADMIN@PLAYPAL.COM", "pass");
        assertTrue(authManager.isAdmin(adminCaps));
    }

    @Test
    void testGetUserById() throws DuplicateEmailException {
        String email = generateUniqueEmail();
        authManager.register("Find Me", email, "pass", false, null, 0);
        
        try {
            User u = authManager.login(email, "pass");
            String fullId = u.getId();

            User found = authManager.getUserById(fullId.substring(0, 8));

            assertNotNull(found);
            assertEquals(email, found.getEmail());
        } catch (Exception e) {
            fail("Should not throw exception during setup");
        }
    }
}