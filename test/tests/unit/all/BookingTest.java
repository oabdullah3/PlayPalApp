package tests.unit.all;

import app.entities.Booking;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testBookingLifecycle() {
        // 1. Constructor Coverage
        String playerId = "player-001";
        String trainerId = "trainer-001";
        double amount = 75.50;
        
        Booking booking = new Booking(playerId, trainerId, amount);

        // 2. Getter Coverage
        assertNotNull(booking.getBookingId(), "Booking ID should be auto-generated");
        assertEquals(playerId, booking.getPlayerId());
        assertEquals(trainerId, booking.getTrainerId());
        assertEquals(amount, booking.getAmount(), 0.001);

        // 3. Setter Coverage
        String newId = "custom-uuid-123";
        booking.setId(newId);
        assertEquals(newId, booking.getBookingId());

        // 4. toDisplayString Coverage
        // Format: [Booking ID: %s] Player: %s | Amount: $%.2f
        String display = booking.toDisplayString("John Doe");
        
        assertTrue(display.contains("cust"), "Should contain the ID substring");
        assertTrue(display.contains("John Doe"), "Should contain player name");
        assertTrue(display.contains("$75.50"), "Should contain formatted amount");
    }
}