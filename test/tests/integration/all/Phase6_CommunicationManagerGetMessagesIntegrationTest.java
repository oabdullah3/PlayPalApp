package tests.integration.all;

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
 * INTEGRATION PHASE 6: CommunicationManager.getMessagesForCurrentUser()
 * 
 * Tests the getMessagesForCurrentUser method which depends on:
 * - AuthManager.getCurrentUser() (Phase 2 tested)
 * - Database (unit tested)
 * - Message entity (unit tested)
 */
public class Phase6_CommunicationManagerGetMessagesIntegrationTest {

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
        
        // Create test users
        Player receiver = new Player("Receiver", "receiver@test.com", "pass");
        Player sender1 = new Player("Sender1", "sender1@test.com", "pass");
        Player sender2 = new Player("Sender2", "sender2@test.com", "pass");
        
        db.getAllUsers().add(receiver);
        db.getAllUsers().add(sender1);
        db.getAllUsers().add(sender2);
        
        // Login receiver
        try {
            authManager.login("receiver@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Login failed");
        }
        
        // Add messages to receiver
        String receiverId = receiver.getId();
        db.getAllMessages().add(new Message(sender1.getId(), receiverId, "Message 1"));
        db.getAllMessages().add(new Message(sender2.getId(), receiverId, "Message 2"));
        db.getAllMessages().add(new Message(sender1.getId(), receiverId, "Message 3"));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testGetMessagesForCurrentUser() {
        List<Message> messages = communicationManager.getMessagesForCurrentUser();
        
        assertEquals(3, messages.size());
    }

    @Test
    void testGetMessagesReturnsSortedByTimestamp() throws InterruptedException {
        Database db = Database.getInstance();
        String receiverId = authManager.getCurrentUser().getId();
        
        db.getAllMessages().clear();
        
        Message msg1 = new Message("sender1", receiverId, "Old");
        Thread.sleep(10); // Ensure different timestamps
        Message msg2 = new Message("sender2", receiverId, "New");
        
        db.getAllMessages().add(msg1);
        db.getAllMessages().add(msg2);
        
        List<Message> messages = communicationManager.getMessagesForCurrentUser();
        
        assertEquals(2, messages.size());
        // Newest should be first (msg2)
        assertEquals("New", messages.get(0).getContent());
        assertEquals("Old", messages.get(1).getContent());
    }

    @Test
    void testGetMessagesWithoutLogin() {
        authManager.logout();
        
        List<Message> messages = communicationManager.getMessagesForCurrentUser();
        
        assertTrue(messages.isEmpty());
    }

    @Test
    void testGetMessagesOnlyForCurrentUser() throws Exception {
        Database db = Database.getInstance();
        Player receiver = (Player) db.getAllUsers().get(0);
        Player other = (Player) db.getAllUsers().get(1);
        
        authManager.logout();
        authManager.login("sender1@test.com", "pass");
        
        // Login as sender1, should have no messages
        List<Message> messages = communicationManager.getMessagesForCurrentUser();
        
        assertTrue(messages.isEmpty());
    }

    @Test
    void testGetMessagesEmpty() {
        Database db = Database.getInstance();
        db.getAllMessages().clear();
        
        List<Message> messages = communicationManager.getMessagesForCurrentUser();
        
        assertTrue(messages.isEmpty());
    }
}
