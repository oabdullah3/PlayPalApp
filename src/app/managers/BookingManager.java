package app.managers;

import app.entities.Booking;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.BookingFailedException;
import app.exceptions.InsufficientFundsException;


import java.util.List;

public class BookingManager {

    private final Database db = Database.getInstance();
    private final AuthManager authManager = AuthManager.getInstance();

    private static BookingManager instance;

    protected BookingManager() {}

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    public List<Trainer> searchApprovedTrainers(String specialty) {
    	return db.findApprovedTrainersBySpecialty(specialty);
    }
    

    public List<Booking> getAllBookingsForTrainer(String trainerId) {
    	return db.findBookingsByTrainerId(trainerId);
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

        if (!(trainerUser instanceof Trainer)) {
            throw new BookingFailedException("User found with ID " + trainerId + ", but they are not a Trainer.");
        }
        
        Trainer trainer = (Trainer) trainerUser;

        if (!trainer.isApproved()) {
            throw new BookingFailedException("Trainer is not yet approved by SystemManager.");
        }

        double totalCost = trainer.getHourlyRate() * hours;
        
        if (player.getBalance() < totalCost) {
            throw new InsufficientFundsException("Booking failed. Required: $" + totalCost + ", Available: $" + player.getBalance());
        }

        player.pay(totalCost);
        trainer.receivePayment(totalCost);
        
        db.addUser(player);
        db.addUser(trainer);

        Booking newBooking = new Booking(player.getId(), trainer.getId(), totalCost);
        db.addBooking(newBooking);
        
        String notificationMessage = String.format("Booking successful! Paid $%.2f. Your new balance is $%.2f.\n", totalCost, player.getBalance());
        System.out.print(notificationMessage);
        CommunicationManager.getInstance().sendNotification(player.getId(), notificationMessage);
    }
}