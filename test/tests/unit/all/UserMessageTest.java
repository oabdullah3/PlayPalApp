package tests.unit.all;

import app.entities.UserMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserMessageTest {

    @Test
    void testUserMessageSpecifics() {
        // Covers: Constructor, getSenderId
        String senderId = "sender-long-id";
        UserMessage msg = new UserMessage(senderId, "receiver1", "Hello");

        assertEquals(senderId, msg.getSenderId());
        assertEquals("receiver1", msg.getReceiverId());
    }

    @Test
    void testToStringFormattingWithSubstring() {
        // Covers: toString (specifically the senderId.substring(0, 4) logic)
        String senderId = "123456789";
        UserMessage msg = new UserMessage(senderId, "rec1", "Hi there");

        String output = msg.toString();

        // Logic Check: Should show only first 4 chars of senderId ("1234")
        assertTrue(output.contains("From 1234:"), "Should contain substring of sender ID");
        assertTrue(output.contains("Hi there"));
    }
    
    // Note: If you want to be robust, you might test what happens if ID < 4 chars, 
    // but the current code would throw an exception. For pure code coverage of existing lines, 
    // the test above is sufficient.
}