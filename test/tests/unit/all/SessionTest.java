package tests.unit.all;

import app.entities.Session;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testSessionInitializationAndGetters() {
        // Covers: Constructor, getters
        LocalDateTime time = LocalDateTime.now();
        Session s = new Session("creator1", "Soccer", "Field A", time, 10);

        assertNotNull(s.getSessionId());
        assertEquals("creator1", s.getCreatorId());
        assertEquals("Soccer", s.getSport());
        assertEquals("Field A", s.getLocation());
        
        // Constructor logic check: Creator is automatically added
        assertEquals(1, s.getParticipantIds().size());
        assertTrue(s.getParticipantIds().contains("creator1"));
    }

    @Test
    void testIsFull_ConditionFalse() {
        // Covers: isFull() -> False branch (Size < Max)
        LocalDateTime time = LocalDateTime.now();
        // Max 3 participants. Initially has 1 (creator).
        Session s = new Session("c1", "Sport", "Loc", time, 3);
        
        assertFalse(s.isFull(), "Session with 1/3 should not be full");
        
        s.addParticipant("p2");
        assertFalse(s.isFull(), "Session with 2/3 should not be full");
    }

    @Test
    void testIsFull_ConditionTrue() {
        // Covers: isFull() -> True branch (Size >= Max)
        LocalDateTime time = LocalDateTime.now();
        // Max 2 participants. Initially has 1 (creator).
        Session s = new Session("c1", "Sport", "Loc", time, 2);
        
        s.addParticipant("p2"); // Now 2/2
        
        assertTrue(s.isFull(), "Session with 2/2 should be full");
    }

    @Test
    void testSettersAndDirectListManipulation() {
        // Covers: setId, setParticipantIds
        LocalDateTime time = LocalDateTime.now();
        Session s = new Session("c1", "Sport", "Loc", time, 5);
        
        s.setId("fixed-id-999");
        assertEquals("fixed-id-999", s.getSessionId());

        List<String> newParticipants = Arrays.asList("a", "b", "c");
        s.setParticipantIds(newParticipants);
        
        assertEquals(3, s.getParticipantIds().size());
        assertTrue(s.getParticipantIds().contains("a"));
    }

    @Test
    void testToStringFormatting() {
        // Covers: toString
        LocalDateTime time = LocalDateTime.of(2025, 12, 25, 14, 30);
        Session s = new Session("c1", "Tennis", "Court 1", time, 4);
        
        // Ensure ID is set to something predictable for the substring check
        s.setId("12345678-uuid"); 
        
        String output = s.toString();
        
        // Expected: "[1234] Tennis at Court 1 (14:30) - Players: 1/4"
        assertTrue(output.contains("[1234]"));
        assertTrue(output.contains("Tennis at Court 1"));
        assertTrue(output.contains("Players: 1/4"));
    }
}