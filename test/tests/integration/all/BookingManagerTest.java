package tests.integration.all;

import app.entities.Booking;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.BookingFailedException;
import app.exceptions.InsufficientFundsException;
import app.managers.AuthManager;
import app.managers.BookingManager;
import app.managers.Database;
import app.managers.SystemManager;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingManagerTest {

    private static BookingManager bookingManager;
    private static AuthManager authManager;
    private static SystemManager systemManager;
    
    private static String playerEmail;
    private static String trainerEmail;
    private static String trainerId;

    @BeforeAll
    static void setupGlobal() {
        bookingManager = BookingManager.getInstance();
        authManager = AuthManager.getInstance();
        systemManager = SystemManager.getInstance();

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        playerEmail = "player_" + suffix + "@book.com";
        trainerEmail = "trainer_" + suffix + "@book.com";

        try {
            authManager.register("Rich Player", playerEmail, "pass", false, null, 0);
            authManager.register("Yoga Coach", trainerEmail, "pass", true, "Yoga", 100.0);
            
            trainerId = authManager.login(trainerEmail, "pass").getId();
            authManager.logout();
        } catch (Exception e) {}
    }

    @BeforeEach
    void clearAuth() {
        authManager.logout();
    }


    @Test
    @Order(1)
    void testBookTrainer_Failure_NotLoggedIn() {
        assertThrows(BookingFailedException.class, () -> 
            bookingManager.bookTrainer(trainerId, 1)
        );
    }

    @Test
    @Order(2)
    void testBookTrainer_Failure_TrainerNotFound() throws Exception {
        authManager.login(playerEmail, "pass");
        
        assertThrows(BookingFailedException.class, () -> 
            bookingManager.bookTrainer("bad-id", 1)
        );
    }

    @Test
    @Order(3)
    void testBookTrainer_Failure_NotATrainer() throws Exception {
        authManager.login(playerEmail, "pass");
        String myId = authManager.getCurrentUser().getId();
        
        Exception e = assertThrows(BookingFailedException.class, () -> 
            bookingManager.bookTrainer(myId, 1)
        );
        assertTrue(e.getMessage().contains("not a Trainer"));
    }

    @Test
    @Order(4)
    void testBookTrainer_Failure_NotApproved() throws Exception {
        authManager.login(playerEmail, "pass");
        
        Exception e = assertThrows(BookingFailedException.class, () -> 
            bookingManager.bookTrainer(trainerId, 1)
        );
        assertTrue(e.getMessage().contains("not yet approved"));
    }

    @Test
    @Order(5)
    void testBookTrainer_Failure_InsufficientFunds() throws Exception {
        systemManager.approveTrainer(trainerId);
        
        authManager.login(playerEmail, "pass");
        
        assertThrows(InsufficientFundsException.class, () -> 
            bookingManager.bookTrainer(trainerId, 1)
        );
    }

    @Test
    @Order(6)
    void testBookTrainer_Success() throws Exception {
        authManager.login(playerEmail, "pass");
        authManager.getCurrentUser().setBalance(200.0);
        
        assertDoesNotThrow(() -> bookingManager.bookTrainer(trainerId, 1)); 

        User p = authManager.getCurrentUser();
        assertEquals(100.0, p.getBalance(), "Should deduct 100");
        
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer(trainerId);
        assertFalse(bookings.isEmpty());
    }

    @Test
    @Order(7)
    void testSearchApprovedTrainers() {
        List<Trainer> trainers = bookingManager.searchApprovedTrainers("Yoga");
        boolean found = trainers.stream().anyMatch(t -> t.getId().equals(trainerId));
        assertTrue(found);
    }
}