package tests.system.all;

import app.main.PlayPalApp;
import app.entities.User;
import app.managers.AuthManager;
import app.managers.Database;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemMessagingTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        AuthManager.getInstance().logout();
        Database.getInstance().getAllMessages().clear();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        try {
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, new Scanner(System.in));
        } catch (Exception e) {
            fail("Failed to reset scanner: " + e.getMessage());
        }
    }

    @Test
    void testSendMessageFlow() {
        String senderEmail = "sender_" + UUID.randomUUID() + "@chat.com";
        String receiverEmail = "receiver_" + UUID.randomUUID() + "@chat.com";

        try {
            AuthManager.getInstance().register("Sender", senderEmail, "pass", false, null, 0);
            AuthManager.getInstance().register("Receiver", receiverEmail, "pass", false, null, 0);
        } catch (Exception e) {}

        User receiver = Database.getInstance().findUserByEmail(receiverEmail);
        String receiverIdPrefix = receiver.getId().substring(0, 5);

        StringBuilder input = new StringBuilder();
        
        input.append("1\n");
        input.append("\n"); 
        input.append(senderEmail + "\n");
        input.append("pass\n");

        input.append("4\n"); 
        input.append(receiverIdPrefix + "\n");
        input.append("Hello from System Test Integration\n"); // Message Content

        input.append("0\n"); // Back to dashboard
        input.append("5\n"); // Logout
        input.append("3\n"); // Exit App

       
        setInput(input.toString());
        
        try {
            PlayPalApp.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fullLog = outContent.toString() + errContent.toString();
        
        boolean logSuccess = fullLog.contains("Message sent successfully");
        
        boolean dbSuccess = Database.getInstance().findMessagesForUser(receiver.getId())
                .stream()
                .anyMatch(m -> m.getContent().equals("Hello from System Test Integration"));
        
        assertTrue(logSuccess || dbSuccess, 
            "Message should be found in receiver's inbox (DB) or confirmed in log.\nFull Log:\n" + fullLog);
    }
}