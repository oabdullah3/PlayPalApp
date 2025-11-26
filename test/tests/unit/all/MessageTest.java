package tests.unit.all;

import app.entities.Message;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testMessageLifecycle() {
        String sender = "user-1";
        String receiver = "user-2";
        String content = "Hello World";

        Message msg = new Message(sender, receiver, content);

        // Verify Construction
        assertNotNull(msg.getMessageId());
        assertNotNull(msg.getTimestamp());
        assertEquals(sender, msg.getSenderId());
        assertEquals(receiver, msg.getReceiverId());
        assertEquals(content, msg.getContent());
        
        // Verify default read status
        assertFalse(msg.isRead());

        // Verify Setter
        msg.setRead(true);
        assertTrue(msg.isRead());
    }

    @Test
    void testToString() {
        Message msg = new Message("sender123", "receiver456", "Test Content");
        String str = msg.toString();

        // Verify formatting includes sender prefix and content
        // Note: ID is substring(0,4) in logic
        assertTrue(str.contains("send")); 
        assertTrue(str.contains("Test Content"));
    }
}