package tests.integration.phase5;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.SessionManager;
import app.managers.AuthManager;
import app.managers.Database;
import app.entities.Session;
import app.entities.Player;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 5: SessionManager.createSession()
 * 
 * Tests the createSession method which depends on:
 * - AuthManager.getCurrentUser() (Phase 2 tested)
 * - Database (unit tested)
 * - Session constructor (unit tested)
 */
public class Phase5_SessionManagerCreateSessionIntegrationTest {

    private SessionManager sessionManager;
    private AuthManager authManager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        authManager = AuthManager.getInstance();
        Database db = Database.getInstance();
        db.getAllSessions().clear();
        db.getAllUsers().clear();
        authManager.logout();
        System.setOut(new PrintStream(outContent));
        
        // Create and login a player
        Player player = new Player("Test Player", "test@test.com", "pass");
        db.getAllUsers().add(player);
        try {
            authManager.login("test@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Login failed");
        }
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testCreateSessionSuccessfully() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        
        assertEquals(1, Database.getInstance().getAllSessions().size());
        Session session = Database.getInstance().getAllSessions().get(0);
        
        assertEquals("Tennis", session.getSport());
        assertEquals("Court 1", session.getLocation());
        assertEquals(1, session.getParticipantIds().size()); // Creator is automatically first participant
        assertTrue(session.getParticipantIds().contains(authManager.getCurrentUser().getId()));
    }

    @Test
    void testCreateMultipleSessions() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        sessionManager.createSession("Soccer", "Field 1", time, 6);
        sessionManager.createSession("Basketball", "Court 2", time, 10);
        
        assertEquals(3, Database.getInstance().getAllSessions().size());
    }

    @Test
    void testCreateSessionWithoutLogin() {
        authManager.logout();
        Database db = Database.getInstance();
        
        int sessionsBefore = db.getAllSessions().size();
        
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        
        // Should not create session if no one is logged in
        assertEquals(sessionsBefore, db.getAllSessions().size());
    }

    @Test
    void testCreateSessionCreatorIsParticipant() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        String playerId = authManager.getCurrentUser().getId();
        
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        
        Session session = Database.getInstance().getAllSessions().get(0);
        
        assertTrue(session.getParticipantIds().contains(playerId));
        assertEquals(playerId, session.getCreatorId());
    }

    @Test
    void testCreateSessionWithDifferentDetails() {
        LocalDateTime time1 = LocalDateTime.now().plusHours(1);
        LocalDateTime time2 = LocalDateTime.now().plusHours(2);
        
        sessionManager.createSession("Tennis", "Court A", time1, 4);
        sessionManager.createSession("Soccer", "Field B", time2, 8);
        
        Database db = Database.getInstance();
        assertEquals(2, db.getAllSessions().size());
        
        Session session1 = db.getAllSessions().get(0);
        Session session2 = db.getAllSessions().get(1);
        
        assertEquals("Tennis", session1.getSport());
        assertEquals("Soccer", session2.getSport());
        assertEquals("Court A", session1.getLocation());
        assertEquals("Field B", session2.getLocation());
    }

    @Test
    void testCreateSessionVerifiesUniqueIds() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        
        sessionManager.createSession("Tennis", "Court 1", time, 4);
        sessionManager.createSession("Tennis", "Court 2", time, 4);
        
        Database db = Database.getInstance();
        String id1 = db.getAllSessions().get(0).getSessionId();
        String id2 = db.getAllSessions().get(1).getSessionId();
        
        assertNotEquals(id1, id2);
    }
}
