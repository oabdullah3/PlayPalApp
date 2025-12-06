package tests.integration.all;

import app.entities.Message;
import app.entities.UserMessage;
import app.managers.Database;
import app.patterns.command.SendMessageCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SendMessageCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final Database db = Database.getInstance();

    @BeforeEach
    void setUp() {
        // Capture Console Output
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Restore Console Output
        System.setOut(originalOut);
    }

    @Test
    void testExecute_SavesToRealDB_AndPrintsLog() {
        // 1. Arrange: Create unique data to ensure we find *this* specific message
        String uniqueSenderId = "Sender-" + UUID.randomUUID().toString().substring(0, 5);
        String receiverId = "Rec-" + UUID.randomUUID().toString();
        String content = "Integration Test Message";
        
        UserMessage message = new UserMessage(uniqueSenderId, receiverId, content);
        SendMessageCommand command = new SendMessageCommand(message);

        // 2. Act: Execute the command (Writes to Real DB)
        command.execute();

        // 3. Assert (Persistence): Query Real DB to confirm it saved
        List<Message> messages = db.findMessagesForUser(receiverId);
        boolean found = false;
        for (Message m : messages) {
            if (m.getMessageId().equals(message.getMessageId())) {
                assertEquals(content, m.getContent());
                found = true;
                break;
            }
        }
        assertTrue(found, "The command should have saved the message to the real database.");

        // 4. Assert (Console): Verify the print statement
        // Logic: substring(0, 4) of uniqueSenderId
        String expectedPrefix = uniqueSenderId.substring(0, 4); 
        String consoleOutput = outContent.toString();
        
        assertTrue(consoleOutput.contains("Message command executed"), "Should print execution log");
        assertTrue(consoleOutput.contains(expectedPrefix), "Should contain sender ID substring");
    }
}