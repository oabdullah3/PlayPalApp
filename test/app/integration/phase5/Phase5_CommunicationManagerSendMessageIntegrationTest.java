package app.integration.phase5;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.CommunicationManager;
import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Message;
import app.entities.Player;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 5: CommunicationManager.sendMessage()
 * 
 * Tests the sendMessage method which depends on:
 * - AuthManager.getCurrentUser() (Phase 2 tested)
 * - SendMessageCommand (unit tested)
 * - Message entity (unit tested)
 */
public class Phase5_CommunicationManagerSendMessageIntegrationTest {

    private CommunicationManager communicationManager;
    private AuthManager authManager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        communicationManager = CommunicationManager.getInstance();
        authManager = AuthManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        db.getAllMessages().clear();
        authManager.logout();
        System.setOut(new PrintStream(outContent));
        
        // Create and login a player
        Player sender = new Player("Sender", "sender@test.com", "pass");
        Player receiver = new Player("Receiver", "receiver@test.com", "pass");
        db.getAllUsers().add(sender);
        db.getAllUsers().add(receiver);
        
        try {
            authManager.login("sender@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Login failed");
        }
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testSendMessageSuccessfully() {
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(1);
        
        communicationManager.sendMessage(receiver.getId(), "Hello!");
        
        assertEquals(1, db.getAllMessages().size());
        Message message = db.getAllMessages().get(0);
        
        assertEquals(authManager.getCurrentUser().getId(), message.getSenderId());
        assertEquals(receiver.getId(), message.getReceiverId());
        assertEquals("Hello!", message.getContent());
    }

    @Test
    void testSendMultipleMessages() {
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(1);
        
        communicationManager.sendMessage(receiver.getId(), "Message 1");
        communicationManager.sendMessage(receiver.getId(), "Message 2");
        communicationManager.sendMessage(receiver.getId(), "Message 3");
        
        assertEquals(3, db.getAllMessages().size());
    }

    @Test
    void testSendMessageWithoutLogin() {
        authManager.logout();
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(1);
        
        int messagesBefore = db.getAllMessages().size();
        communicationManager.sendMessage(receiver.getId(), "Hello!");
        
        assertEquals(messagesBefore, db.getAllMessages().size());
    }

    @Test
    void testSendMessageStoresCorrectData() {
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(1);
        String senderId = authManager.getCurrentUser().getId();
        String content = "Test Message Content";
        
        communicationManager.sendMessage(receiver.getId(), content);
        
        Message message = db.getAllMessages().get(0);
        assertEquals(senderId, message.getSenderId());
        assertEquals(receiver.getId(), message.getReceiverId());
        assertEquals(content, message.getContent());
        assertFalse(message.isRead());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testSendMessageVerifiesUniqueness() {
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(1);
        
        communicationManager.sendMessage(receiver.getId(), "Message");
        communicationManager.sendMessage(receiver.getId(), "Message");
        
        assertEquals(2, db.getAllMessages().size());
        String id1 = db.getAllMessages().get(0).getMessageId();
        String id2 = db.getAllMessages().get(1).getMessageId();
        
        assertNotEquals(id1, id2);
    }
}
