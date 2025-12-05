package tests.unit.all;

import app.entities.Message;
import app.entities.Notification; // Used as a concrete implementation to test base logic
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testMessageInitialization() {
        // Covers: Constructor initialization (UUID generation, timestamp, isRead=false)
        Message msg = new Notification("receiver123", "Test Content");

        assertNotNull(msg.getMessageId(), "Message ID should be auto-generated");
        assertNotNull(msg.getTimestamp(), "Timestamp should be auto-generated");
        assertEquals("receiver123", msg.getReceiverId());
        assertEquals("Test Content", msg.getContent());
        assertFalse(msg.isRead(), "Message should default to unread");
    }

    @Test
    void testSettersAndStateChanges() {
        // Covers: setId, setRead, isRead
        Message msg = new Notification("r1", "c1");

        // Test ID Setter
        msg.setId("custom-uuid-999");
        assertEquals("custom-uuid-999", msg.getMessageId());

        // Test Read Status State Change
        msg.setRead(true);
        assertTrue(msg.isRead());
        
        msg.setRead(false);
        assertFalse(msg.isRead());
    }
}