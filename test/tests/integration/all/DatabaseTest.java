package tests.integration.all;

import app.entities.*;
import app.managers.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseTest {

    private static Database db;

    private static final String UNIQUE_SUFFIX = UUID.randomUUID().toString().substring(0, 5);
    private static final String PLAYER_EMAIL = "test_player_" + UNIQUE_SUFFIX + "@example.com";
    private static final String TRAINER_EMAIL = "test_trainer_" + UNIQUE_SUFFIX + "@example.com";
    
    private static String createdPlayerId;
    private static String createdTrainerId;
    private static String createdSessionId;

    @BeforeAll
    static void setup() {
        System.out.println("--- Starting Integration Tests on REAL Database ---");
        db = Database.getInstance();
    }

    @Test
    @Order(1)
    void testCreateAndRetrievePlayer() {
        Player p = new Player("Integration Player", PLAYER_EMAIL, "pass123");
        p.setBalance(150.00);
        
        db.addUser(p);
        createdPlayerId = p.getId();

        User retrieved = db.findUserByEmail(PLAYER_EMAIL);
        
        assertNotNull(retrieved, "Should find the user we just saved");
        assertEquals(createdPlayerId, retrieved.getId());
        assertEquals(150.00, retrieved.getBalance(), 0.01);
        assertTrue(retrieved instanceof Player);
    }

    @Test
    @Order(2)
    void testCreateAndRetrieveTrainer() {
        Trainer t = new Trainer("Integration Coach", TRAINER_EMAIL, "pass123", "BasketWeaving", 99.0);
        t.setApproved(false);
        
        db.addUser(t);
        createdTrainerId = t.getId();
        
        User retrieved = db.findUserByIdPrefix(createdTrainerId.substring(0, 4));
        
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof Trainer);
        assertEquals("BasketWeaving", ((Trainer) retrieved).getSpecialty());
        assertFalse(((Trainer) retrieved).isApproved());
    }

    @Test
    @Order(3)
    void testTrainerApprovalUpdate() {
        List<Trainer> pending = db.findPendingTrainers();
        boolean found = pending.stream().anyMatch(t -> t.getId().equals(createdTrainerId));
        assertTrue(found, "Trainer should appear in pending list");

        db.updateTrainerApproval(createdTrainerId, true);

        User u = db.findUserByEmail(TRAINER_EMAIL);
        assertTrue(((Trainer) u).isApproved(), "Trainer should now be approved in DB");
        
        List<Trainer> approved = db.findApprovedTrainersBySpecialty("BasketWeaving");
        boolean foundApproved = approved.stream().anyMatch(t -> t.getId().equals(createdTrainerId));
        assertTrue(foundApproved, "Should be found in approved specialty search");
    }
    
    @Test
    @Order(4)
    void testEmailExists() {
        assertTrue(db.emailExists(PLAYER_EMAIL));
        assertFalse(db.emailExists("non_existent_" + UNIQUE_SUFFIX + "@fake.com"));
    }


    @Test
    @Order(5)
    void testSessionLifecycle() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5);
        Session s = new Session(createdTrainerId, "ExtremeIroning", "Garage", futureTime, 5);
        
        db.addSession(s);
        createdSessionId = s.getSessionId();

        List<Session> available = db.findAvailableSessionsBySport("ExtremeIroning");
        boolean found = available.stream().anyMatch(sess -> sess.getSessionId().equals(createdSessionId));
        assertTrue(found, "New session should be available");
        
        Session retrieved = db.findSessionByIdPrefix(createdSessionId.substring(0, 5));
        assertNotNull(retrieved);
        assertEquals("Garage", retrieved.getLocation());
    }

    @Test
    @Order(6)
    void testAddParticipantDirectly() {
        db.addParticipantDirectly(createdSessionId, createdPlayerId);

        Session s = db.findSessionByIdPrefix(createdSessionId);
        
        assertTrue(s.getParticipantIds().contains(createdPlayerId), "Player ID should be in participant list");
    }


    @Test
    @Order(7)
    void testBookingPersistence() {
        Booking b = new Booking(createdPlayerId, createdTrainerId, 50.0);
        db.addBooking(b);

        List<Booking> bookings = db.findBookingsByTrainerId(createdTrainerId);
        boolean exists = bookings.stream().anyMatch(book -> book.getBookingId().equals(b.getBookingId()));
        
        assertTrue(exists, "Booking should be saved and linked to trainer");
    }


    @Test
    @Order(8)
    void testMessageSystemPolymorphism() {
        UserMessage um = new UserMessage(createdPlayerId, createdTrainerId, "Hello Coach");
        db.addMessage(um);

        Notification note = new Notification(createdTrainerId, "System Alert");
        db.addMessage(note);

        List<Message> messages = db.findMessagesForUser(createdTrainerId);
        
        boolean foundUserMsg = false;
        boolean foundNotif = false;

        for (Message m : messages) {
            if (m.getMessageId().equals(um.getMessageId())) {
                assertTrue(m instanceof UserMessage);
                assertEquals("Hello Coach", m.getContent());
                foundUserMsg = true;
            }
            if (m.getMessageId().equals(note.getMessageId())) {
                assertTrue(m instanceof Notification);
                assertEquals("System Alert", m.getContent());
                foundNotif = true;
            }
        }

        assertTrue(foundUserMsg, "Should successfully retrieve UserMessage type");
        assertTrue(foundNotif, "Should successfully retrieve Notification type");
    }
    

    @Test
    @Order(9)
    void testUpdateSession() {
        Session s = db.findSessionByIdPrefix(createdSessionId);
        s.addParticipant("ghost-player");
        
        db.updateSession(s); 
        
        Session updated = db.findSessionByIdPrefix(createdSessionId);
        assertTrue(updated.getParticipantIds().contains("ghost-player"));
    }

    @Test
    @Order(10)
    void testGetAllMessages() {
        List<Message> allMsgs = db.getAllMessages();
        assertNotNull(allMsgs);
        assertFalse(allMsgs.isEmpty(), "Should contain the messages created in previous steps");
    }

    @Test
    @Order(11)
    void testUserNotFoundScenarios() {
        User u1 = db.findUserByEmail("non.existent.ghost@example.com");
        assertNull(u1, "Should return null for non-existent email");

        User u2 = db.findUserByIdPrefix("zzzzzz");
        assertNull(u2, "Should return null for non-existent prefix");
    }

    @Test
    @Order(12)
    void testSessionIsFullFilter() {
        Session session = new Session(createdTrainerId, "SoloChess", "Room 101", LocalDateTime.now().plusDays(1), 2);
        db.addSession(session);
        String sId = session.getSessionId();
        
        List<Session> available = db.findAvailableSessionsBySport("SoloChess");
        boolean isThere = available.stream().anyMatch(s -> s.getSessionId().equals(sId));
        assertTrue(isThere, "Session with 1/2 capacity should be available");

        db.addParticipantDirectly(sId, "player-2");
        
        List<Session> filtered = db.findAvailableSessionsBySport("SoloChess");
        boolean isGone = filtered.stream().noneMatch(s -> s.getSessionId().equals(sId));
        
        assertTrue(isGone, "Full session (2/2) should be filtered out");
    }

    @Test
    @Order(13)
    void testUnknownMessageType() {
        Message alienMsg = new Message("receiver-1", "Alien Content") {
            @Override
            public String toString() { return "Alien"; }
        };
        
        db.addMessage(alienMsg);
        
        List<Message> msgs = db.findMessagesForUser("receiver-1");
        boolean foundAlien = msgs.stream().anyMatch(m -> "Alien Content".equals(m.getContent()));
        
        assertTrue(foundAlien, "Should handle unknown Message subclasses gracefully");
    }
}