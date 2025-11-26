package tests.unit.all;

import app.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    static class TestUser extends User {
        public TestUser(String name, String email, String password, double initialBalance) {
            super(name, email, password, initialBalance);
        }

        @Override
        public void showMenuOptions() {
            
        }
    }

    @Test
    void testConstructorAndGetters() {
        User user = new TestUser("John Doe", "john@test.com", "secret", 100.0);

        assertNotNull(user.getId(), "ID should be auto-generated");
        assertEquals("John Doe", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(100.0, user.getBalance());
    }

    @Test
    void testSetters() {
        User user = new TestUser("Old Name", "email", "pass", 50.0);
        
        user.showMenuOptions();

        user.setName("New Name");
        assertEquals("New Name", user.getName());

        user.setBalance(200.50);
        assertEquals(200.50, user.getBalance());
    }
}