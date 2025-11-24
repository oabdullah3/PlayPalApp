package app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    
    private final String bookingId;
    private final String playerId;
    private final String trainerId;
    private final double amount;
    private final LocalDateTime timestamp;
    private String status; // e.g., "CONFIRMED", "COMPLETED", "CANCELED"

    public Booking(String playerId, String trainerId, double amount) {
        this.bookingId = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.trainerId = trainerId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = "CONFIRMED";
    }

    // --- Getters ---

    public String getBookingId() {
        return bookingId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public double getAmount() {
        return amount;
    }
}