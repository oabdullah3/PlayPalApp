package app.ui;

import java.time.LocalDateTime;
import java.util.List;

import app.entities.Player;
import app.entities.Session;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.BookingFailedException;
import app.exceptions.InsufficientFundsException;
import app.exceptions.SessionFullException;
import app.exceptions.SessionNotFoundException;
import app.managers.AuthManager;
import app.managers.BookingManager;
import app.managers.SessionManager;
import app.utils.InputValidator;

public class PlayerUI implements UserUI {
	
	private final SessionManager sessionManager = SessionManager.getInstance();
	private final AuthManager authManager = AuthManager.getInstance();
    private final BookingManager bookingManager = BookingManager.getInstance();
    private final SharedUI sharedUI = new SharedUI();
	
	public void run(int choice) {
        switch (choice) {
            case 1:
                handleCreateSession();
                break;
            case 2:
                handleJoinSession();
                break;
            case 3:
                handleSearchTrainers();
                break;
            case 4:
                sharedUI.handleViewMessages();
                break;
            case 5:
                authManager.logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    private void handleCreateSession() {
        String sport = InputValidator.readString("Sport (e.g., Basketball): ");
        String location = InputValidator.readString("Location: ");
        LocalDateTime time = InputValidator.readDateTime("Time");
        int max = InputValidator.readInt("Max Participants: ");
        
        sessionManager.createSession(sport, location, time, max);
    }

    private void handleJoinSession() {
        String sport = InputValidator.readString("Search sport: ");
        List<Session> sessions = sessionManager.searchAvailableSessions(sport);
        
        if (sessions.isEmpty()) {
            System.out.println("No sessions found for " + sport + ".");
            return;
        }

        System.out.println("\n--- Available Sessions ---");
        sessions.forEach(s -> System.out.println(s.getSessionId().substring(0, 4) + " | " + s.toString()));
        
        String sessionId = InputValidator.readString("Enter Session ID prefix to join: ");
        
        try {
            sessionManager.joinSession(sessionId);
        } catch (SessionNotFoundException | SessionFullException e) {
            System.err.println("Join Failed: " + e.getMessage());
        }
    }
    
    private void handleSearchTrainers() {
        String specialty = InputValidator.readString("Enter specialty (e.g., Yoga): ");
        List<Trainer> trainers = bookingManager.searchApprovedTrainers(specialty);
        
        if (trainers.isEmpty()) {
            System.out.println("No approved trainers found for " + specialty + ".");
            return;
        }

        System.out.println("\n--- Available Trainers ---");
        trainers.forEach(t -> System.out.printf("[%s] %s ($%.2f/hr)\n", t.getId().substring(0, 4), t.getName(), t.getHourlyRate()));
        
        String trainerId = InputValidator.readString("Enter Trainer ID prefix to book: ");
        int hours = InputValidator.readInt("Enter number of hours (integer): ");
        
        try {
            bookingManager.bookTrainer(trainerId, hours);
        } catch (InsufficientFundsException | BookingFailedException e) {
            System.err.println("Booking Failed: " + e.getMessage());
        }
    }
	
}