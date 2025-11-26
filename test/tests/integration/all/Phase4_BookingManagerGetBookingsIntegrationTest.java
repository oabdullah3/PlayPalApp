package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.managers.BookingManager;
import app.managers.Database;
import app.entities.Booking;
import java.util.List;

/**
 * INTEGRATION PHASE 4: BookingManager.getAllBookingsForTrainer()
 * 
 * Tests the getAllBookingsForTrainer method which depends on:
 * - Database (unit tested)
 * - Booking entity (unit tested)
 */
public class Phase4_BookingManagerGetBookingsIntegrationTest {

    private BookingManager bookingManager;

    @BeforeEach
    void setUp() {
        bookingManager = BookingManager.getInstance();
        Database db = Database.getInstance();
        db.getAllBookings().clear();
        
        // Create bookings for different trainers
        Booking booking1 = new Booking("player1", "trainer1", 100.0);
        Booking booking2 = new Booking("player2", "trainer1", 150.0);
        Booking booking3 = new Booking("player3", "trainer2", 200.0);
        Booking booking4 = new Booking("player4", "trainer1", 120.0);
        
        db.getAllBookings().add(booking1);
        db.getAllBookings().add(booking2);
        db.getAllBookings().add(booking3);
        db.getAllBookings().add(booking4);
    }

    @Test
    void testGetAllBookingsForTrainer() {
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer("trainer1");
        
        assertEquals(3, bookings.size());
        assertTrue(bookings.stream().allMatch(b -> b.getTrainerId().equals("trainer1")));
    }

    @Test
    void testGetBookingsForDifferentTrainer() {
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer("trainer2");
        
        assertEquals(1, bookings.size());
        assertEquals("trainer2", bookings.get(0).getTrainerId());
    }

    @Test
    void testGetBookingsForTrainerWithNoBookings() {
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer("trainer3");
        
        assertTrue(bookings.isEmpty());
    }

    @Test
    void testGetBookingsEmptyDatabase() {
        Database.getInstance().getAllBookings().clear();
        
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer("trainer1");
        
        assertTrue(bookings.isEmpty());
    }

    @Test
    void testGetBookingsVerifyAmounts() {
        List<Booking> bookings = bookingManager.getAllBookingsForTrainer("trainer1");
        
        assertEquals(3, bookings.size());
        double totalAmount = bookings.stream().mapToDouble(Booking::getAmount).sum();
        assertEquals(370.0, totalAmount, 0.01);
    }
}
