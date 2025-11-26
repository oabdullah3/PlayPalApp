package tests.unit.managers;

import app.managers.Database;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {
    @Test
    void testSingletonAndGetters() {
        Database db1 = Database.getInstance();
        Database db2 = Database.getInstance();
        assertSame(db1, db2);

        // Verify lists are initialized
        assertNotNull(db1.getAllUsers());
        assertNotNull(db1.getAllSessions());
        assertNotNull(db1.getAllBookings());
        assertNotNull(db1.getAllMessages());
        
        // Verify Mock Data exists (Constructor logic)
        assertFalse(db1.getAllUsers().isEmpty());
    }
}