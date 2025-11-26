package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.SessionManager;
import app.managers.Database;
import app.entities.Session;
import java.time.LocalDateTime;
import java.util.List;

/**
 * INTEGRATION PHASE 4: SessionManager.getSessionById() & searchAvailableSessions()
 * 
 * Tests the session query methods which depend on:
 * - Database (unit tested)
 * - Session entity (unit tested)
 */
public class Phase4_SessionManagerQueryIntegrationTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        Database db = Database.getInstance();
        db.getAllSessions().clear();
        
        LocalDateTime time = LocalDateTime.now().plusHours(2);
        
        // Create test sessions
        Session session1 = new Session("creator1", "Tennis", "Court 1", time, 4);
        Session session2 = new Session("creator2", "Soccer", "Field 1", time, 6);
        Session session3 = new Session("creator3", "Tennis", "Court 2", time, 2);
        
        // Fill session 3 to test isFull()
        // Session 3 has creator as first participant (1/2), add one more to make it full (2/2)
        session3.addParticipant("player1");
        
        db.getAllSessions().add(session1);
        db.getAllSessions().add(session2);
        db.getAllSessions().add(session3);
    }

    @Test
    void testGetSessionById() {
        Database db = Database.getInstance();
        Session expectedSession = db.getAllSessions().get(0);
        String sessionId = expectedSession.getSessionId().substring(0, 8);
        
        Session foundSession = sessionManager.getSessionById(sessionId);
        
        assertNotNull(foundSession);
        assertEquals(expectedSession.getSessionId(), foundSession.getSessionId());
    }

    @Test
    void testGetSessionByIdNotFound() {
        Session foundSession = sessionManager.getSessionById("nonexistent");
        
        assertNull(foundSession);
    }

    @Test
    void testSearchAvailableSessions() {
        List<Session> sessions = sessionManager.searchAvailableSessions("Tennis");
        
        // Should return 1 available Tennis session (session1)
        // session3 is full and should not be included
        assertEquals(1, sessions.size());
        assertTrue(sessions.stream().allMatch(s -> s.getSport().equalsIgnoreCase("Tennis")));
        assertTrue(sessions.stream().allMatch(s -> !s.isFull()));
    }

    @Test
    void testSearchAvailableSessionsExcludesFull() {
        List<Session> sessions = sessionManager.searchAvailableSessions("Tennis");
        
        // Session 3 is full, should not be included in results
        // Only session1 (not full) should be returned
        assertEquals(1, sessions.size());
        assertTrue(sessions.stream().noneMatch(s -> s.getSessionId().equals(
            Database.getInstance().getAllSessions().get(2).getSessionId()
        )));
    }

    @Test
    void testSearchAvailableSessionsDifferentSport() {
        List<Session> sessions = sessionManager.searchAvailableSessions("Soccer");
        
        assertEquals(1, sessions.size());
        assertEquals("Soccer", sessions.get(0).getSport());
    }

    @Test
    void testSearchAvailableSessionsNoResults() {
        List<Session> sessions = sessionManager.searchAvailableSessions("Basketball");
        
        assertTrue(sessions.isEmpty());
    }

    @Test
    void testSearchAvailableSessionsCaseInsensitive() {
        List<Session> sessions1 = sessionManager.searchAvailableSessions("tennis");
        List<Session> sessions2 = sessionManager.searchAvailableSessions("TENNIS");
        
        assertEquals(sessions1.size(), sessions2.size());
        assertEquals(1, sessions1.size());
    }

    @Test
    void testGetSessionByIdEmptyDatabase() {
        Database.getInstance().getAllSessions().clear();
        
        Session foundSession = sessionManager.getSessionById("anyid");
        
        assertNull(foundSession);
    }
}
