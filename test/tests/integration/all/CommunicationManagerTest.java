package tests.integration.all;

import app.entities.Message;
import app.managers.AuthManager;
import app.managers.CommunicationManager;
import app.managers.Database;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommunicationManagerTest {

    private static CommunicationManager comms;
    private static AuthManager auth;
    private static Database db;

    private static String senderEmail;
    private static String receiverEmail;
    private static String receiverId;
    
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeAll
    static void setupGlobal() {
        comms = CommunicationManager.getInstance();
        auth = AuthManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        senderEmail = "comm_sender_" + suffix + "@test.com";
        receiverEmail = "comm_receiver_" + suffix + "@test.com";

        try {
            auth.register("Sender", senderEmail, "pass", false, null, 0);
            auth.register("Receiver", receiverEmail, "pass", false, null, 0);
            
            // Get Receiver ID
            receiverId = auth.login(receiverEmail, "pass").getId();
            auth.logout(); 
        } catch (Exception e) { fail("Setup failed"); }
    }

    @BeforeEach
    void setupStream() {
        System.setErr(new PrintStream(errContent));
        auth.logout();
    }

    @AfterEach
    void restoreStream() {
        System.setErr(originalErr);
    }


    @Test
    @Order(1)
    void testSendMessage_Failure_NotLoggedIn() {
        comms.sendMessage(receiverId, "Fail Msg");
        assertTrue(errContent.toString().contains("Error: Must be logged in"));
    }

    @Test
    @Order(2)
    void testSendMessage_Success() throws Exception {
        auth.login(senderEmail, "pass");
        
        String content = "Hello Logic Check " + UUID.randomUUID();
        comms.sendMessage(receiverId, content);

        List<Message> msgs = db.findMessagesForUser(receiverId);
        boolean found = msgs.stream().anyMatch(m -> m.getContent().equals(content));
        assertTrue(found, "Message should be saved via Command execution");
    }


    @Test
    @Order(3)
    void testGetMessages_Failure_NotLoggedIn() {
        // Covers: getMessagesForCurrentUser -> if (user == null)
        List<Message> msgs = comms.getMessagesForCurrentUser();
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty());
    }

    @Test
    @Order(4)
    void testGetMessages_Success() throws Exception {
        auth.login(receiverEmail, "pass");
        
        List<Message> msgs = comms.getMessagesForCurrentUser();
        assertFalse(msgs.isEmpty(), "Receiver should have messages from Test #2");
    }

    @Test
    @Order(5)
    void testSendNotification() {
        String content = "System Alert " + UUID.randomUUID();
        comms.sendNotification(receiverId, content);

        List<Message> msgs = db.findMessagesForUser(receiverId);
        boolean found = msgs.stream().anyMatch(m -> m.getContent().equals(content));
        assertTrue(found);
    }
}