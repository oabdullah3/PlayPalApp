package tests.unit.all;

import app.entities.Player;
import app.entities.User;
import app.managers.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class ManagerAccessTest {

    /**
     * Helper method to force-reset a Singleton instance to null via Reflection.
     * This ensures we can test the "Creation" branch even if other tests ran first.
     */
    private void resetSingleton(Class<?> clazz, String fieldName) {
        try {
            Field instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset singleton for " + clazz.getSimpleName(), e);
        }
    }

    @Test
    void testAuthManagerSingleton() {
        // 1. Force reset to ensure we test the "Creation" branch (if == null)
        resetSingleton(AuthManager.class, "instance");
        
        // First call: Should enter the 'if' block and create instance
        AuthManager first = AuthManager.getInstance();
        assertNotNull(first);

        // Second call: Should skip the 'if' block and return existing instance
        AuthManager second = AuthManager.getInstance();
        assertSame(first, second, "Should return the same instance");
    }

    @Test
    void testSessionManagerSingleton() {
        resetSingleton(SessionManager.class, "instance");
        
        SessionManager first = SessionManager.getInstance();
        assertNotNull(first);
        
        SessionManager second = SessionManager.getInstance();
        assertSame(first, second);
    }

    @Test
    void testBookingManagerSingleton() {
        resetSingleton(BookingManager.class, "instance");
        
        BookingManager first = BookingManager.getInstance();
        assertNotNull(first);
        
        BookingManager second = BookingManager.getInstance();
        assertSame(first, second);
    }

    @Test
    void testCommunicationManagerSingleton() {
        resetSingleton(CommunicationManager.class, "instance");
        
        CommunicationManager first = CommunicationManager.getInstance();
        assertNotNull(first);
        
        CommunicationManager second = CommunicationManager.getInstance();
        assertSame(first, second);
    }

    @Test
    void testSystemManagerSingleton() {
        resetSingleton(SystemManager.class, "instance");
        
        SystemManager first = SystemManager.getInstance();
        assertNotNull(first);
        
        SystemManager second = SystemManager.getInstance();
        assertSame(first, second);
    }
    
    @Test
    void testDatabaseSingleton() {
        resetSingleton(Database.class, "instance");
        
        Database first = Database.getInstance();
        assertNotNull(first);
        
        Database second = Database.getInstance();
        assertSame(first, second);
    }

    @Test
    void testSystemManagerDisplay() {
        // displaySystemStatus only prints to console and reads List.size()
        // It is safe to test as a leaf function
        assertDoesNotThrow(() -> SystemManager.getInstance().displaySystemStatus());
    }
    
    @Test
    void testAuthManagerCurrentUserDefault() {
        // By default, no one should be logged in
        assertNull(AuthManager.getInstance().getCurrentUser());
    }

    @Test
    void testAuthManagerLogout() throws Exception {
        AuthManager auth = AuthManager.getInstance();

        // 1. SETUP: Manually inject a "Fake" Logged-in User via Reflection
        // We do this to avoid calling the Phase 2 'login()' method, keeping this a Unit Test.
        User fakeUser = new Player("Fake", "fake@test.com", "pw");
        
        Field userField = AuthManager.class.getDeclaredField("currentUser");
        userField.setAccessible(true);
        userField.set(auth, fakeUser);

        // Verify injection worked
        assertNotNull(auth.getCurrentUser());
        assertEquals("Fake", auth.getCurrentUser().getName());

        // 2. EXECUTE: Call the function under test
        auth.logout();

        // 3. ASSERT: Verify state was cleared
        assertNull(auth.getCurrentUser(), "Logout should set currentUser to null");
    }
}