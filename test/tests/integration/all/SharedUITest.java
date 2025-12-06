package tests.integration.all;

import app.entities.Message;
import app.entities.User;
import app.managers.AuthManager;
import app.managers.CommunicationManager;
import app.managers.Database;
import app.ui.SharedUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SharedUITest {

    private static SharedUI sharedUI;
    private static AuthManager authManager;
    private static Database db;
    
    private static String senderEmail;
    private static String receiverEmail;
    private static String senderId;
    private static String receiverId;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void setupGlobal() {
        sharedUI = new SharedUI();
        authManager = AuthManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        senderEmail = "s_" + suffix + "@ui.com";
        receiverEmail = "r_" + suffix + "@ui.com";

        try {
            authManager.register("UI Sender", senderEmail, "pass", false, null, 0);
            authManager.register("UI Receiver", receiverEmail, "pass", false, null, 0);
            
            senderId = authManager.login(senderEmail, "pass").getId();
            authManager.logout();
            receiverId = authManager.login(receiverEmail, "pass").getId();
            authManager.logout();
        } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() {
        authManager.logout(); 
        System.setOut(new PrintStream(outContent)); 
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private void provideInput(String data) {
        try {
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, mockScanner);
        } catch (Exception e) { fail(e.getMessage()); }
    }

    @Test
    @Order(1)
    void testHandleViewMessages_NoUser() {
        outContent.reset();
        
        sharedUI.handleViewMessages();
        
        assertEquals("", outContent.toString().trim());
    }

    @Test
    @Order(2)
    void testHandleViewMessages_SendMessage_Success() throws Exception {
        outContent.reset();
        authManager.login(senderEmail, "pass");

        String inputs = receiverId.substring(0, 6) + "\nHello from UI\n";
        provideInput(inputs);

        sharedUI.handleViewMessages();

        assertTrue(outContent.toString().contains("Message sent successfully!"));

        // Verify Persistence
        List<Message> msgs = db.findMessagesForUser(receiverId);
        boolean found = msgs.stream().anyMatch(m -> m.getContent().equals("Hello from UI"));
        assertTrue(found);
    }

    @Test
    @Order(3)
    void testHandleViewMessages_WithHistory() throws Exception {
        authManager.login(receiverEmail, "pass");
        CommunicationManager.getInstance().sendMessage(senderId, "History Message 101");
        authManager.logout(); 

        outContent.reset();
        authManager.login(senderEmail, "pass");
        
        provideInput("0\n");

        sharedUI.handleViewMessages();

        assertTrue(outContent.toString().contains("History Message 101"), 
            "Should print existing messages in the forEach loop");
    }

    @Test
    @Order(4)
    void testHandleViewMessages_CancelSend() throws Exception {
        authManager.login(senderEmail, "pass");
        provideInput("0\n"); 

        sharedUI.handleViewMessages();

        assertFalse(outContent.toString().contains("Enter message content"));
    }

    @Test
    @Order(5)
    void testHandleViewMessages_UserNotFound() throws Exception {
        authManager.login(senderEmail, "pass");
        
        provideInput("NON_EXISTENT_" + UUID.randomUUID() + "\n");

        sharedUI.handleViewMessages();

        assertTrue(outContent.toString().contains("User not found with that ID prefix"));
    }
}