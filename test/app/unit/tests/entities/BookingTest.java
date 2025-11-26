package app.unit.tests.entities;

import app.entities.Booking;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testBookingFunctionality() {
        String playerId = "player-123";
        String trainerId = "trainer-456";
        double amount = 75.0;

        Booking booking = new Booking(playerId, trainerId, amount);

        assertNotNull(booking.getBookingId());
        assertFalse(booking.getBookingId().isEmpty());

        assertEquals(playerId, booking.getPlayerId());
        assertEquals(trainerId, booking.getTrainerId());
        assertEquals(amount, booking.getAmount());
    }
}