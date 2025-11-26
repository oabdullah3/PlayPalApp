package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.CommunicationManager;
import app.managers.Database;
import app.patterns.command.Command;
import app.patterns.command.NotificationCommand;
import app.patterns.command.SendMessageCommand;
import app.entities.Message;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 4: CommunicationManager.send()
 * 
 * Tests the send method which depends on:
 * - Command interface (unit tested)
 * - NotificationCommand (unit tested)
 * - SendMessageCommand (unit tested)
 */
public class Phase4_CommunicationManagerSendIntegrationTest {

    private CommunicationManager communicationManager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        communicationManager = CommunicationManager.getInstance();
        Database.getInstance().getAllMessages().clear();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testSendNotificationCommand() {
        Command notificationCmd = new NotificationCommand("user123", "Test Alert");
        
        communicationManager.send(notificationCmd);
        
        String output = outContent.toString();
        assertTrue(output.contains("ALERT"));
        assertTrue(output.contains("Test Alert"));
    }

    @Test
    void testSendMessageCommand() {
        Message message = new Message("sender1", "receiver1", "Hello World");
        Command sendMessageCmd = new SendMessageCommand(message);
        
        communicationManager.send(sendMessageCmd);
        
        String output = outContent.toString();
        assertTrue(output.contains("Message command executed"));
    }

    @Test
    void testSendMultipleCommands() {
        Command cmd1 = new NotificationCommand("user1", "Alert 1");
        Command cmd2 = new NotificationCommand("user2", "Alert 2");
        
        communicationManager.send(cmd1);
        communicationManager.send(cmd2);
        
        String output = outContent.toString();
        assertTrue(output.contains("Alert 1"));
        assertTrue(output.contains("Alert 2"));
    }

    @Test
    void testSendVerifiesMessageStorage() {
        Message message = new Message("sender1", "receiver1", "Test Message");
        Command cmd = new SendMessageCommand(message);
        
        int messageCountBefore = Database.getInstance().getAllMessages().size();
        communicationManager.send(cmd);
        int messageCountAfter = Database.getInstance().getAllMessages().size();
        
        assertEquals(messageCountAfter, messageCountBefore + 1);
    }
}
