package tests.unit.all;

import app.entities.Notification;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void testNotificationConstructorAndToString() {
        // Covers: Constructor, toString
        Notification note = new Notification("player1", "Your booking is confirmed");

        String output = note.toString();
        
        // Logic Check: Format is "[Time] From [System]: Content"
        // We verify the static parts and the content flow
        assertTrue(output.contains("From [System]"), "Notification must appear from [System]");
        assertTrue(output.contains("Your booking is confirmed"), "Content must be present");
        
        assertTrue(output.matches("\\[\\d{2}:\\d{2}.*\\].*"), "Should start with time format like [14:30:05]");    }
}