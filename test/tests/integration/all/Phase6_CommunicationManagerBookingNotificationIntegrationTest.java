package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.CommunicationManager;
import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Player;
import app.entities.Trainer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 6: CommunicationManager.sendBookingNotification()
 * 
 * Tests the sendBookingNotification method which depends on:
 * - AuthManager.getUserById() (Phase 2 tested)
 * - NotificationCommand (unit tested)
 * - CommunicationManager.send() (Phase 4 tested)
 */
public class Phase6_CommunicationManagerBookingNotificationIntegrationTest {

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
        System.setOut(new PrintStream(outContent));
        
        // Create player and trainer
        Player player = new Player("Test Player", "player@test.com", "pass");
        Trainer trainer = new Trainer("Test Trainer", "trainer@test.com", "pass", "Tennis", 50.0);
        
        db.getAllUsers().add(player);
        db.getAllUsers().add(trainer);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testSendBookingNotification() {
        Database db = Database.getInstance();
        String playerId = db.getAllUsers().get(0).getId();
        String trainerId = db.getAllUsers().get(1).getId();
        double cost = 150.0;
        
        communicationManager.sendBookingNotification(playerId, trainerId, cost);
        
        String output = outContent.toString();
        assertTrue(output.contains("ALERT"));
        assertTrue(output.contains("150"));
    }

    @Test
    void testSendBookingNotificationNotifiesBothParties() {
        Database db = Database.getInstance();
        String playerId = db.getAllUsers().get(0).getId();
        String trainerId = db.getAllUsers().get(1).getId();
        
        communicationManager.sendBookingNotification(playerId, trainerId, 100.0);
        
        String output = outContent.toString();
        // Should have two ALERT notifications (one for player, one for trainer)
        long alertCount = output.split("ALERT").length - 1;
        assertEquals(2, alertCount);
    }

    @Test
    void testSendBookingNotificationIncludesPlayerInfo() {
        Database db = Database.getInstance();
        Player player = (Player) db.getAllUsers().get(0);
        String trainerId = db.getAllUsers().get(1).getId();
        
        communicationManager.sendBookingNotification(player.getId(), trainerId, 75.0);
        
        String output = outContent.toString();
        assertTrue(output.contains("Test Player"));
    }

    @Test
    void testSendBookingNotificationIncludesTrainerInfo() {
        Database db = Database.getInstance();
        String playerId = db.getAllUsers().get(0).getId();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        
        communicationManager.sendBookingNotification(playerId, trainer.getId(), 75.0);
        
        String output = outContent.toString();
        assertTrue(output.contains("Test Trainer"));
    }

    @Test
    void testSendBookingNotificationIncludesCost() {
        Database db = Database.getInstance();
        String playerId = db.getAllUsers().get(0).getId();
        String trainerId = db.getAllUsers().get(1).getId();
        
        communicationManager.sendBookingNotification(playerId, trainerId, 250.50);
        
        String output = outContent.toString();
        assertTrue(output.contains("250.50"));
    }

    @Test
    void testSendMultipleBookingNotifications() {
        Database db = Database.getInstance();
        String playerId = db.getAllUsers().get(0).getId();
        String trainerId = db.getAllUsers().get(1).getId();
        
        communicationManager.sendBookingNotification(playerId, trainerId, 100.0);
        communicationManager.sendBookingNotification(playerId, trainerId, 200.0);
        
        String output = outContent.toString();
        assertTrue(output.contains("100"));
        assertTrue(output.contains("200"));
    }
}
