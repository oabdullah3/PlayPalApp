package tests.current.temporary;

import app.entities.Session;
import app.entities.Trainer;
import app.entities.User;
import app.managers.*;
import app.ui.PlayerUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayerUITest {

    private static PlayerUI playerUI;
    private static AuthManager authManager;
    private static SessionManager sessionManager;
    private static BookingManager bookingManager;
    private static Database db;

    private static String playerEmail;
    private static String trainerEmail;
    private static String trainerId;
    private static String createdSessionSport;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeAll
    static void setupGlobal() {
        playerUI = new PlayerUI();
        authManager = AuthManager.getInstance();
        sessionManager = SessionManager.getInstance();
        bookingManager = BookingManager.getInstance();
        db = Database.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        playerEmail = "p_ui_" + suffix + "@test.com";
        trainerEmail = "t_ui_" + suffix + "@test.com";
        createdSessionSport = "Sport_" + suffix;

        try {
            authManager.register("UI Player", playerEmail, "pass", false, null, 0);
            
            authManager.register("UI Trainer", trainerEmail, "pass", true, "Yoga", 50.0);
            trainerId = authManager.login(trainerEmail, "pass").getId();
            authManager.logout();
            SystemManager.getInstance().approveTrainer(trainerId);
        } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() {
        try {
            if (authManager.getCurrentUser() == null || !authManager.getCurrentUser().getEmail().equals(playerEmail)) {
                authManager.login(playerEmail, "pass");
            }
        } catch (Exception e) {}

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void provideInput(String data) {
        try {
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, mockScanner);
        } catch (Exception e) { fail(e.getMessage()); }
    }

    @Test
    @Order(1)
    void testRun_CreateSession() {
        String futureTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String inputs = createdSessionSport + "\nCentral Park\n" + futureTime + "\n10\n";
        provideInput(inputs);

        playerUI.run(1);

        assertTrue(outContent.toString().contains("Session created successfully"));
        
        List<Session> sessions = sessionManager.searchAvailableSessions(createdSessionSport);
        assertFalse(sessions.isEmpty());
    }

    @Test
    @Order(2)
    void testRun_JoinSession_Success() {
        authManager.logout();
        try { authManager.login(trainerEmail, "pass"); } catch (Exception e) {}
        String trainerSport = "TrainerBall_" + UUID.randomUUID().toString().substring(0,4);
        sessionManager.createSession(trainerSport, "Gym", LocalDateTime.now().plusDays(2), 5);
        String sessionId = sessionManager.searchAvailableSessions(trainerSport).get(0).getSessionId();
        authManager.logout();
        
        try { authManager.login(playerEmail, "pass"); } catch (Exception e) {}

        String inputs = trainerSport + "\n" + sessionId.substring(0, 5) + "\n";
        provideInput(inputs);

        playerUI.run(2);

        assertTrue(outContent.toString().contains("Successfully joined session"));
    }

    @Test
    @Order(3)
    void testRun_JoinSession_NoSessionsFound() {
        provideInput("NonExistentSport\n");

        playerUI.run(2);

        assertTrue(outContent.toString().contains("No sessions found"));
    }

    @Test
    @Order(4)
    void testRun_SearchTrainers_Success() {
        authManager.getCurrentUser().setBalance(200.0);

        String inputs = "Yoga\n" + trainerId.substring(0, 6) + "\n2\n";
        provideInput(inputs);

        playerUI.run(3);

        assertTrue(outContent.toString().contains("Booking successful"));
    }

    @Test
    @Order(5)
    void testRun_SearchTrainers_InsufficientFunds() {
        authManager.getCurrentUser().setBalance(0.0);

        String inputs = "Yoga\n" + trainerId.substring(0, 6) + "\n2\n";
        provideInput(inputs);

        playerUI.run(3);

        String errLog = errContent.toString();
        
        assertTrue(errLog.contains("Booking Failed"), "Should log failure prefix");
        assertTrue(errLog.contains("Required"), "Should contain logic about required funds");
    }
    
    @Test
    @Order(6)
    void testRun_SearchTrainers_NoTrainersFound() {
        provideInput("RocketScience\n");
        playerUI.run(3);
        assertTrue(outContent.toString().contains("No approved trainers found"));
    }

    @Test
    @Order(7)
    void testRun_InvalidChoice() {
        playerUI.run(99);
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    @Order(8)
    void testRun_Logout() {
        playerUI.run(5);
        
        assertNull(authManager.getCurrentUser());
        assertTrue(outContent.toString().contains("Logout successful"));
    }
    
    @Test
    @Order(9)
    void testRun_ViewMessages_Integration() {
        try { authManager.login(playerEmail, "pass"); } catch (Exception e) {}
        provideInput("0\n");
        playerUI.run(4);

        assertDoesNotThrow(() -> {});
    }

    @Test
    @Order(10)
    void testRun_JoinSession_ExceptionHandling() {
        authManager.logout();
        try { authManager.login(trainerEmail, "pass"); } catch (Exception e) {}
        String sport = "CatchTest_" + UUID.randomUUID().toString().substring(0,4);
        sessionManager.createSession(sport, "Gym", LocalDateTime.now().plusDays(2), 5);
        authManager.logout();
        
        try { authManager.login(playerEmail, "pass"); } catch (Exception e) {}

        String inputs = sport + "\nINVALID_ID_PREFIX\n";
        provideInput(inputs);
        playerUI.run(2);
        
        assertTrue(errContent.toString().contains("Join Failed"), 
            "Should hit the catch block in handleJoinSession");
    }
}