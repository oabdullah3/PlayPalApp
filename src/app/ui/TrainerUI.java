package app.ui;

import java.util.List;

import app.entities.Booking;
import app.entities.Trainer;
import app.entities.User;
import app.managers.AuthManager;
import app.managers.BookingManager;
import app.utils.InputValidator;

public class TrainerUI {
	
	private final AuthManager authManager = AuthManager.getInstance();
    private final BookingManager bookingManager = BookingManager.getInstance();
    private final SharedUI sharedUI = new SharedUI();
	
    public void handleTrainerChoice(int choice, Trainer trainer) {
        switch (choice) {
            case 1: 
            	handleViewBookingsForTrainer(trainer);
                break;
            case 2: 
            	handleUpdateProfileForTrainer(trainer);
                break;
            case 3: 
                sharedUI.handleViewMessages();
                break;
            case 4:
                authManager.logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    

    private void handleViewBookingsForTrainer(Trainer trainer) {
        List<Booking> trainerBookings = bookingManager.getAllBookingsForTrainer(trainer.getId());

        if (trainerBookings.isEmpty()) {
            System.out.println("\n--- Trainer Bookings ---");
            System.out.println("No active or completed bookings found.");
            return;
        }

        System.out.println("\n--- Trainer Bookings for " + trainer.getName() + " ---");
        for (int i = 0; i < trainerBookings.size(); i++) {
            Booking b = trainerBookings.get(i);
            User player = authManager.getUserById(b.getPlayerId());
            String playerName = player != null ? player.getName() : "Unknown Player";

            System.out.println((i + 1) + ". " + b.toDisplayString(playerName));
        }
    }
    

    private void handleUpdateProfileForTrainer(Trainer trainer) {
        System.out.println("\n--- Update Trainer Profile ---");
        System.out.println("Current Name: " + trainer.getName());
        String newName = InputValidator.readOptionalString("Enter new name (or press Enter to keep current): ");

        System.out.printf("Current Rate: $%.2f/hr\n", trainer.getHourlyRate());
        String newRateStr = InputValidator.readOptionalString("Enter new hourly rate (or press Enter to keep current): ");

        System.out.println("Current Specialty: " + trainer.getSpecialty());
        String newSpecialty = InputValidator.readOptionalString("Enter new specialty (or press Enter to keep current): ");
        
        
        if (!newName.isEmpty()) {
            trainer.setName(newName); 
        }
        
        if (!newRateStr.isEmpty()) {
            try {
                double newRate = Double.parseDouble(newRateStr);
                trainer.setHourlyRate(newRate);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rate format. Rate remains unchanged.");
            }
        }
        
        if (!newSpecialty.isEmpty()) {
            trainer.setSpecialty(newSpecialty);
        }

        System.out.println("Profile updated successfully!");
    }
	
}