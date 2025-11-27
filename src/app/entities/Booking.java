package app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    
    private final String bookingId;
    private final String playerId;
    private final String trainerId;
    private final double amount;

    public Booking(String playerId, String trainerId, double amount) {
        this.bookingId = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.trainerId = trainerId;
        this.amount = amount;
    }
    
    public String toDisplayString(String playerName) {
        return String.format("[Booking ID: %s] Player: %s | Amount: $%.2f",
            bookingId.substring(0, 4), 
            playerName, 
            amount);
    }

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