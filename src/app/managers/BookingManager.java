package app.managers;

import app.entities.Booking;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.BookingFailedException;
import app.exceptions.InsufficientFundsException;


import java.util.List;
import java.util.stream.Collectors;

public class BookingManager {

    private final Database db = Database.getInstance();
    private final AuthManager authManager = AuthManager.getInstance();

    // --- Singleton-like access ---
    private static BookingManager instance;

    protected BookingManager() {}

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }
    // -----------------------------

    // --- Core Methods ---

    public List<Trainer> searchApprovedTrainers(String specialty) {
        return db.getAllUsers().stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .filter(t -> t.isApproved() && t.getSpecialty().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }
    

    public List<Booking> getAllBookingsForTrainer(String trainerId) {
        return db.getAllBookings().stream()
                 .filter(b -> b.getTrainerId().equals(trainerId))
                 .collect(Collectors.toList());
    }

    public void bookTrainer(String trainerId, int hours) throws InsufficientFundsException, BookingFailedException {
        User player = authManager.getCurrentUser();
        if (player == null) {
            throw new BookingFailedException("Player must be logged in to book a trainer.");
        }
        
        User trainerUser = authManager.getUserById(trainerId); 

        if (trainerUser == null) {
            throw new BookingFailedException("Trainer ID prefix not found.");
        }

        // 2. Safely validate the type before casting
        if (!(trainerUser instanceof Trainer)) {
            // This handles the case where the ID prefix points to a regular Player
            throw new BookingFailedException("User found with ID " + trainerId + ", but they are not a Trainer.");
        }
        
        // 3. Perform the safe cast once validation is complete
        Trainer trainer = (Trainer) trainerUser;

        if (!trainer.isApproved()) {
            throw new BookingFailedException("Trainer is not yet approved by SystemManager.");
        }

        double totalCost = trainer.getHourlyRate() * hours;
        
        // 1. Check Mock Funds
        if (player.getBalance() < totalCost) {
            throw new InsufficientFundsException("Booking failed. Required: $" + totalCost + ", Available: $" + player.getBalance());
        }

        // 2. Process Transaction (Mock)
        
        // Deduct from Player
        player.setBalance(player.getBalance() - totalCost);
        
        // Credit to Trainer
        trainer.setBalance(trainer.getBalance() + totalCost);

        // 3. Create Booking Record
        Booking newBooking = new Booking(player.getId(), trainer.getId(), totalCost);
        db.getAllBookings().add(newBooking);

        System.out.printf("Booking successful! Paid $%.2f. Your new balance is $%.2f.\n", totalCost, player.getBalance());
        
        // Trigger notifications
        CommunicationManager.getInstance().sendBookingNotification(player.getId(), trainer.getId(), totalCost);
    }
}