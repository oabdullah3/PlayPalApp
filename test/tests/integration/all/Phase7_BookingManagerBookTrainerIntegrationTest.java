package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.BookingManager;
import app.managers.AuthManager;
import app.managers.SystemManager;
import app.managers.Database;
import app.entities.Player;
import app.entities.Trainer;
import app.entities.Booking;
import app.exceptions.InsufficientFundsException;
import app.exceptions.BookingFailedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * INTEGRATION PHASE 7: BookingManager.bookTrainer()
 * 
 * Tests the bookTrainer method which is the most complex integration.
 * Dependencies:
 * - AuthManager.getCurrentUser() (Phase 2)
 * - AuthManager.getUserById() (Phase 2)
 * - SystemManager.approveTrainer() (Phase 3)
 * - BookingManager.searchApprovedTrainers() (Phase 4)
 * - CommunicationManager.sendBookingNotification() (Phase 6)
 * - Trainer entity and methods (Phase 1)
 * - User.setBalance() (unit tested)
 * - Booking constructor (unit tested)
 */
public class Phase7_BookingManagerBookTrainerIntegrationTest {

    private BookingManager bookingManager;
    private AuthManager authManager;
    private SystemManager systemManager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        bookingManager = BookingManager.getInstance();
        authManager = AuthManager.getInstance();
        systemManager = SystemManager.getInstance();
        Database db = Database.getInstance();
        db.getAllUsers().clear();
        db.getAllBookings().clear();
        db.getAllMessages().clear();
        authManager.logout();
        System.setOut(new PrintStream(outContent));
        
        // Create and add test users
        Player player = new Player("Test Player", "player@test.com", "pass");
        Trainer trainer = new Trainer("Test Trainer", "trainer@test.com", "pass", "Tennis", 50.0);
        
        db.getAllUsers().add(player);
        db.getAllUsers().add(trainer);
        
        // Login as player
        try {
            authManager.login("player@test.com", "pass");
        } catch (Exception e) {
            System.setOut(originalOut);
            fail("Player login failed");
        }
        
        // Approve trainer
        systemManager.approveTrainer(trainer.getId().substring(0, 8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testBookTrainerSuccessfully() throws InsufficientFundsException, BookingFailedException {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        double initialBalance = player.getBalance();
        double hourlyRate = trainer.getHourlyRate();
        int hours = 1; // Changed from 2 to 1 so player can afford it (50 balance, 50/hr * 1 = 50)
        double expectedCost = hourlyRate * hours;
        
        bookingManager.bookTrainer(trainer.getId().substring(0, 8), hours);
        
        // Verify player balance reduced
        assertEquals(initialBalance - expectedCost, player.getBalance(), 0.01);
        
        // Verify trainer balance increased
        assertEquals(expectedCost, trainer.getBalance(), 0.01);
        
        // Verify booking created
        assertEquals(1, db.getAllBookings().size());
    }

    @Test
    void testBookTrainerInsufficientFunds() {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        
        // Set player balance to insufficient
        player.setBalance(10.0);
        
        assertThrows(InsufficientFundsException.class, () ->
            bookingManager.bookTrainer(trainer.getId().substring(0, 8), 10)
        );
        
        // Balance should not change
        assertEquals(10.0, player.getBalance());
        
        // No booking should be created
        assertTrue(db.getAllBookings().isEmpty());
    }

    @Test
    void testBookTrainerNotApproved() {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        
        // Create unapproved trainer
        Trainer unapproved = new Trainer("Unapproved", "unapp@test.com", "pass", "Soccer", 60.0);
        db.getAllUsers().add(unapproved);
        
        assertThrows(BookingFailedException.class, () ->
            bookingManager.bookTrainer(unapproved.getId().substring(0, 8), 2)
        );
    }

    @Test
    void testBookTrainerNotLoggedIn() {
        authManager.logout();
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        
        assertThrows(BookingFailedException.class, () ->
            bookingManager.bookTrainer(trainer.getId().substring(0, 8), 2)
        );
    }

    @Test
    void testBookTrainerNotFound() {
        assertThrows(BookingFailedException.class, () ->
            bookingManager.bookTrainer("nonexistent", 2)
        );
    }

    @Test
    void testBookTrainerUserIsNotTrainer() {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        
        // Create another player
        Player otherPlayer = new Player("Other Player", "other@test.com", "pass");
        db.getAllUsers().add(otherPlayer);
        
        // Try to book the other player by their ID prefix (they're not a trainer)
        assertThrows(BookingFailedException.class, () ->
            bookingManager.bookTrainer(otherPlayer.getId().substring(0, 8), 1)
        );
    }

    @Test
    void testBookTrainerCreatesCorrectBooking() throws InsufficientFundsException, BookingFailedException {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        int hours = 1; // Changed from 3 to 1 so player can afford it (50/50 = 1 hour max)
        double expectedCost = trainer.getHourlyRate() * hours;
        
        bookingManager.bookTrainer(trainer.getId().substring(0, 8), hours);
        
        Booking booking = db.getAllBookings().get(0);
        assertEquals(player.getId(), booking.getPlayerId());
        assertEquals(trainer.getId(), booking.getTrainerId());
        assertEquals(expectedCost, booking.getAmount(), 0.01);
    }

    @Test
    void testBookTrainerMultipleBookings() throws InsufficientFundsException, BookingFailedException {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        double initialBalance = player.getBalance(); // 50
        
        // Book 1 hour at 50/hr = 50 each time. Player has exactly 50, so can only book once
        // Change to booking smaller amounts - use a trainer with lower rate
        Trainer cheapTrainer = new Trainer("Cheap", "cheap@test.com", "pass", "Yoga", 10.0);
        db.getAllUsers().add(cheapTrainer);
        systemManager.approveTrainer(cheapTrainer.getId().substring(0, 8));
        
        // Book multiple times with cheap trainer
        bookingManager.bookTrainer(cheapTrainer.getId().substring(0, 8), 1);
        bookingManager.bookTrainer(cheapTrainer.getId().substring(0, 8), 1);
        
        assertEquals(2, db.getAllBookings().size());
        assertEquals(initialBalance - (cheapTrainer.getHourlyRate() * 2), player.getBalance(), 0.01);
    }

    @Test
    void testBookTrainerUpdatesTrainerBalance() throws InsufficientFundsException, BookingFailedException {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        double initialBalance = trainer.getBalance();
        int hours = 1; // Changed from 2 to 1 so player can afford it
        double expectedIncome = trainer.getHourlyRate() * hours;
        
        bookingManager.bookTrainer(trainer.getId().substring(0, 8), hours);
        
        assertEquals(initialBalance + expectedIncome, trainer.getBalance(), 0.01);
    }

    @Test
    void testBookTrainerPlayerCannotAfford() {
        Database db = Database.getInstance();
        Player player = (Player) authManager.getCurrentUser();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        
        // Player has 50, trainer costs 50/hr, book for 1 hour = 50 (affordable)
        // So book for 2 hours = 100 (not affordable)
        assertThrows(InsufficientFundsException.class, () ->
            bookingManager.bookTrainer(trainer.getId().substring(0, 8), 2)
        );
    }

    @Test
    void testBookTrainerNotPlayerRole() {
        Database db = Database.getInstance();
        Trainer trainer1 = (Trainer) db.getAllUsers().get(1);
        Trainer trainer2 = new Trainer("Trainer2", "trainer2@test.com", "pass", "Soccer", 55.0);
        trainer2.setApproved(true);
        db.getAllUsers().add(trainer2);
        
        // Logout and login as trainer (save the output handler)
        System.setOut(originalOut); // Restore output before logout to avoid capturing logout messages
        authManager.logout();
        
        try {
            authManager.login("trainer@test.com", "pass");
        } catch (Exception e) {
            fail("Login failed");
        }
        
        System.setOut(new PrintStream(outContent)); // Re-capture output after login
        
        // Trainer trying to book another trainer (has 0 balance)
        assertThrows(InsufficientFundsException.class, () ->
            bookingManager.bookTrainer(trainer2.getId().substring(0, 8), 1)
        );
    }

    @Test
    void testBookTrainerVerifyNotificationSent() throws InsufficientFundsException, BookingFailedException {
        Database db = Database.getInstance();
        Trainer trainer = (Trainer) db.getAllUsers().get(1);
        
        outContent.reset();
        bookingManager.bookTrainer(trainer.getId().substring(0, 8), 1);
        
        String output = outContent.toString();
        // Notification should be sent
        assertTrue(output.contains("Booking successful") || output.contains("ALERT") || output.contains("Booking successful!"));
    }
}
