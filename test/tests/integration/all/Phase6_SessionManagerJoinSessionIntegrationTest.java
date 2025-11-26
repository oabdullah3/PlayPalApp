package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.SessionManager;
import app.managers.CommunicationManager;
import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Session;
import app.entities.Player;
import app.exceptions.SessionNotFoundException;
import app.exceptions.SessionFullException;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 6: SessionManager.joinSession()
 * 
 * Tests the joinSession method which depends on:
 * - AuthManager.getCurrentUser() (Phase 2 tested)
 * - SessionManager.getSessionById() (Phase 4 tested)
 * - CommunicationManager.sendSessionUpdateNotification() (Phase 6 tested)
 * - Session.addParticipant() and isFull() (unit tested)
 */
public class Phase6_SessionManagerJoinSessionIntegrationTest {

    private SessionManager sessionManager;
    private CommunicationManager communicationManager;
    private AuthManager authManager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        communicationManager = CommunicationManager.getInstance();
        authManager = AuthManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        db.getAllSessions().clear();
        db.getAllMessages().clear();
        authManager.logout();
        System.setOut(new PrintStream(outContent));
        
        // Create test players
        Player creator = new Player("Creator", "creator@test.com", "pass");
        Player joiner = new Player("Joiner", "joiner@test.com", "pass");
        db.getAllUsers().add(creator);
        db.getAllUsers().add(joiner);
        
        // Login as creator and create session
        try {
            authManager.login("creator@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Creator login failed");
        }
        
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        
        // Switch to joiner
        authManager.logout();
        try {
            authManager.login("joiner@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Joiner login failed");
        }
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testJoinSessionSuccessfully() throws SessionNotFoundException, SessionFullException {
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        
        int participantsBefore = session.getParticipantIds().size();
        
        sessionManager.joinSession(sessionId);
        
        int participantsAfter = session.getParticipantIds().size();
        assertEquals(participantsBefore + 1, participantsAfter);
    }

    @Test
    void testJoinSessionAddsCurrentUserAsParticipant() throws SessionNotFoundException, SessionFullException {
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        String joinerId = authManager.getCurrentUser().getId();
        
        sessionManager.joinSession(sessionId);
        
        assertTrue(session.getParticipantIds().contains(joinerId));
    }

    @Test
    void testJoinSessionThrowsSessionNotFound() {
        assertThrows(SessionNotFoundException.class, () ->
            sessionManager.joinSession("nonexistent")
        );
    }

    @Test
    void testJoinSessionThrowsSessionFull() throws SessionNotFoundException, SessionFullException {
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        
        // Fill the session (max 4, creator is 1, need to add 3 more)
        session.addParticipant("player1");
        session.addParticipant("player2");
        session.addParticipant("player3");
        
        assertThrows(SessionFullException.class, () ->
            sessionManager.joinSession(sessionId)
        );
    }

    @Test
    void testJoinSessionWithoutLogin() {
        authManager.logout();
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        
        int participantsBefore = session.getParticipantIds().size();
        
        // Should not throw, just silently fail when not logged in
        sessionManager.joinSession(sessionId);
        
        // Session should not have new participant since user was not logged in
        assertEquals(participantsBefore, session.getParticipantIds().size());
    }

    @Test
    void testJoinSameSessionTwice() throws SessionNotFoundException, SessionFullException {
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        
        sessionManager.joinSession(sessionId);
        int participantsAfter1 = session.getParticipantIds().size();
        
        // Try to join again (should not add duplicate)
        sessionManager.joinSession(sessionId);
        int participantsAfter2 = session.getParticipantIds().size();
        
        assertEquals(participantsAfter1, participantsAfter2);
    }

    @Test
    void testJoinSessionSendNotification() throws SessionNotFoundException, SessionFullException {
        Database db = Database.getInstance();
        Session session = db.getAllSessions().get(0);
        String sessionId = session.getSessionId().substring(0, 8);
        
        db.getAllMessages().clear();
        
        sessionManager.joinSession(sessionId);
        
        // A notification should be sent (stored as a message if using notifications)
        // The implementation uses NotificationCommand which prints to console
        String output = outContent.toString();
        assertTrue(output.contains("Successfully joined session") || output.contains("ALERT"));
    }
}
