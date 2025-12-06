package tests.integration.all;

import app.entities.Session;
import app.entities.User;
import app.exceptions.SessionFullException;
import app.exceptions.SessionNotFoundException;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SessionManager;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionManagerTest {

    private static SessionManager sessionManager;
    private static AuthManager authManager;
    private static Database db;

    private static String creatorEmail;
    private static String joinerEmail;
    private static String createdSessionId;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void setupGlobal() {
        sessionManager = SessionManager.getInstance();
        authManager = AuthManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        creatorEmail = "creator_" + suffix + "@sess.com";
        joinerEmail = "joiner_" + suffix + "@sess.com";

        try {
            authManager.register("Creator", creatorEmail, "pass", false, null, 0);
            authManager.register("Joiner", joinerEmail, "pass", false, null, 0);
        } catch (Exception e) {}
    }

    @BeforeEach
    void setupStreams() {
        System.setErr(new PrintStream(errContent));
        System.setOut(new PrintStream(outContent));
        authManager.logout();
    }

    @AfterEach
    void restoreStreams() {
        System.setErr(originalErr);
        System.setOut(originalOut);
    }


    @Test
    @Order(1)
    void testCreateSession_Failure_NotLoggedIn() {
        sessionManager.createSession("Tennis", "Court 1", LocalDateTime.now(), 2);
        assertTrue(errContent.toString().contains("Must be logged in"));
    }

    @Test
    @Order(2)
    void testCreateSession_Success() throws Exception {
        authManager.login(creatorEmail, "pass");

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        sessionManager.createSession("Tennis", "Court 1", time, 2);

        String output = outContent.toString();
        assertTrue(output.contains("Session created successfully"));
        
        List<Session> sessions = sessionManager.searchAvailableSessions("Tennis");
        assertFalse(sessions.isEmpty());
        createdSessionId = sessions.get(0).getSessionId();
    }


    @Test
    @Order(3)
    void testJoinSession_Failure_NotLoggedIn() {
        assertDoesNotThrow(() -> sessionManager.joinSession(createdSessionId));
        assertTrue(errContent.toString().contains("Must be logged in"));
    }

    @Test
    @Order(4)
    void testJoinSession_Failure_NotFound() throws Exception {
        authManager.login(joinerEmail, "pass");
        
        assertThrows(SessionNotFoundException.class, () -> 
            sessionManager.joinSession("invalid-id-prefix")
        );
    }

    @Test
    @Order(5)
    void testJoinSession_Success() throws Exception {
        authManager.login(joinerEmail, "pass");
        
        assertDoesNotThrow(() -> sessionManager.joinSession(createdSessionId));
        
        assertTrue(outContent.toString().contains("Successfully joined"));
        
        User creator = authManager.getUserById(db.findSessionByIdPrefix(createdSessionId).getCreatorId());
        boolean hasNotif = db.findMessagesForUser(creator.getId()).stream()
                .anyMatch(m -> m.getContent().contains("has joined your session"));
        assertTrue(hasNotif, "Creator should receive notification via CommunicationManager");
    }

    @Test
    @Order(6)
    void testJoinSession_Failure_AlreadyJoined() throws Exception {
        authManager.login(joinerEmail, "pass");
        
        sessionManager.joinSession(createdSessionId);
        
        assertTrue(outContent.toString().contains("already participating"));
    }

    @Test
    @Order(7)
    void testJoinSession_Failure_Full() throws Exception {
        String thirdUserEmail = "third" + UUID.randomUUID() + "@test.com";
        authManager.register("Third", thirdUserEmail, "pass", false, null, 0);
        authManager.login(thirdUserEmail, "pass");

        assertThrows(SessionFullException.class, () -> 
            sessionManager.joinSession(createdSessionId)
        );
    }
    
    @Test
    @Order(8)
    void testGetSessionById() {
        assertNotNull(sessionManager.getSessionById(createdSessionId));
        assertNull(sessionManager.getSessionById("garbage"));
    }
}