package app.unit.tests.entities;

import app.entities.Session;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testConstructorAndGetters() {
        String creatorId = "creator-001";
        LocalDateTime time = LocalDateTime.now();
        Session session = new Session(creatorId, "Soccer", "Central Park", time, 10);

        assertNotNull(session.getSessionId());
        assertEquals(creatorId, session.getCreatorId());
        assertEquals("Soccer", session.getSport());
        assertEquals("Central Park", session.getLocation());
        
        // Verify Creator is automatically added as first participant
        assertEquals(1, session.getParticipantIds().size());
        assertEquals(creatorId, session.getParticipantIds().get(0));
    }

    @Test
    void testAddParticipant() {
        Session session = new Session("c1", "Sport", "Loc", LocalDateTime.now(), 10);
        
        session.addParticipant("player-2");
        
        assertEquals(2, session.getParticipantIds().size());
        assertTrue(session.getParticipantIds().contains("player-2"));
    }

    @Test
    void testIsFull() {
        // Create session with capacity of 2
        Session session = new Session("c1", "Sport", "Loc", LocalDateTime.now(), 2);
        
        // Currently has 1 (creator) -> Not Full
        assertFalse(session.isFull());

        // Add 2nd person -> Full (2/2)
        session.addParticipant("p2");
        assertTrue(session.isFull());
    }

    @Test
    void testToString() {
        Session session = new Session("c1", "Tennis", "Court 1", LocalDateTime.now(), 5);
        String result = session.toString();

        // Verify string contains key details
        assertTrue(result.contains("Tennis"));
        assertTrue(result.contains("Court 1"));
        // Format should allow for substring checking
        assertNotNull(result); 
    }
}