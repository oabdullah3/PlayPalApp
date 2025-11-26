package tests.integration.phase6;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.CommunicationManager;
import app.managers.Database;
import app.patterns.command.NotificationCommand;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 6: CommunicationManager.sendSessionUpdateNotification()
 * 
 * Tests the sendSessionUpdateNotification method which depends on:
 * - NotificationCommand (unit tested)
 * - CommunicationManager.send() (Phase 4 tested)
 */
public class Phase6_CommunicationManagerSessionNotificationIntegrationTest {

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
    void testSendSessionUpdateNotification() {
        String userId = "user-123";
        String sessionId = "session-456";
        String content = "New player joined";
        
        communicationManager.sendSessionUpdateNotification(userId, sessionId, content);
        
        String output = outContent.toString();
        assertTrue(output.contains("ALERT"));
        assertTrue(output.contains("Session"));
        assertTrue(output.contains(content));
    }

    @Test
    void testSendSessionUpdateNotificationContent() {
        communicationManager.sendSessionUpdateNotification("user1", "sess1", "Player Alice joined");
        
        String output = outContent.toString();
        assertTrue(output.contains("Player Alice joined"));
    }

    @Test
    void testSendMultipleSessionNotifications() {
        communicationManager.sendSessionUpdateNotification("user1", "sess1", "Notification 1");
        communicationManager.sendSessionUpdateNotification("user2", "sess2", "Notification 2");
        
        String output = outContent.toString();
        assertTrue(output.contains("Notification 1"));
        assertTrue(output.contains("Notification 2"));
    }

    @Test
    void testSendSessionUpdateNotificationWithDifferentContents() {
        communicationManager.sendSessionUpdateNotification("user1", "sess1", "Player joined");
        String output1 = outContent.toString();
        
        outContent.reset();
        
        communicationManager.sendSessionUpdateNotification("user2", "sess2", "Session starting");
        String output2 = outContent.toString();
        
        assertTrue(output1.contains("Player joined"));
        assertTrue(output2.contains("Session starting"));
    }
}
