package tests.integration.all;

import app.entities.Message;
import app.entities.Notification;
import app.managers.Database;
import app.patterns.command.NotificationCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final Database db = Database.getInstance();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testExecute_SavesNotificationToRealDB_AndPrintsSystemLog() {
        // 1. Arrange
        String receiverId = "Rec-" + UUID.randomUUID().toString();
        String content = "System Alert Test";
        
        Notification notification = new Notification(receiverId, content);
        NotificationCommand command = new NotificationCommand(notification);

        // 2. Act
        command.execute();

        // 3. Assert (Persistence): Query Real DB
        List<Message> messages = db.findMessagesForUser(receiverId);
        boolean found = false;
        for (Message m : messages) {
            if (m instanceof Notification && m.getContent().equals(content)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "The command should have saved the notification to the real database.");

        // 4. Assert (Console)
        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.contains("[SYSTEM]"), "Log must mention [SYSTEM]");
        assertTrue(consoleOutput.contains("saved"), "Log must indicate success");
    }
}