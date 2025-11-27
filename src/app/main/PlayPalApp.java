package app.main;

import app.entities.*;
import app.exceptions.*;
import app.managers.*;
import app.utils.*;
import app.patterns.command.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayPalApp {

    // Manager Instances
    private final AuthManager authManager = AuthManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingManager bookingManager = BookingManager.getInstance();
    private final CommunicationManager communicationManager = CommunicationManager.getInstance();
    private final SystemManager systemManager = SystemManager.getInstance(); // Used for Admin tasks

    public static void main(String[] args) {
        System.out.println("--- Welcome to PlayPal CLI ---");
        // Initialize the app and ensure mock data is loaded
        PlayPalApp app = new PlayPalApp();
        app.runMainMenu();
    }

    private void runMainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            
            int choice = InputValidator.readInt("Enter choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        User loggedInUser = handleLogin();
                        if (loggedInUser != null) {
                            runDashboard(loggedInUser);
                        }
                        break;
                    case 2:
                        handleRegistration();
                        break;
                    case 3:
                        running = false;
                        System.out.println("Thank you for using PlayPal. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }
    
    // --- Authentication Flow ---

    private User handleLogin() {
        String email = InputValidator.readString("Enter email: ");
        String password = InputValidator.readString("Enter password: ");
        try {
            return authManager.login(email, password);
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            System.err.println("Login Failed: " + e.getMessage());
            return null;
        }
    }

    private void handleRegistration() {
        String name = InputValidator.readString("Enter name: ");
        String email = InputValidator.readString("Enter email: ");
        String password = InputValidator.readString("Enter password: ");
        
        System.out.println("Register as (1) Player or (2) Trainer? ");
        int typeChoice = InputValidator.readInt("Enter choice: ");
        
        boolean isTrainer = typeChoice == 2;
        String specialty = null;
        double hourlyRate = 0.0;
        
        if (isTrainer) {
            specialty = InputValidator.readString("Enter specialty (e.g., Yoga): ");
            hourlyRate = InputValidator.readDouble("Enter hourly rate (e.g., 25.50): ");
        }

        try {
            authManager.register(name, email, password, isTrainer, specialty, hourlyRate);
        } catch (DuplicateEmailException e) {
            System.err.println("Registration Failed: " + e.getMessage());
        }
    }

    // --- Dashboard Routing ---
    
    private void runDashboard(User user) {
        // Check for system manager role (Admin Logic is handled implicitly by ID for simplicity)
    	if (authManager.isAdmin(user)) {
            runAdminDashboard();
            return;
       }
        
        // Use the State Pattern to display the appropriate menu based on User type
        while (authManager.getCurrentUser() != null) {
            user.showMenuOptions();
            int choice = InputValidator.readInt("Enter choice: ");
            
            if (user instanceof Player) {
                handlePlayerChoice(choice, (Player) user);
            } else if (user instanceof Trainer) {
                handleTrainerChoice(choice, (Trainer) user);
            }
        }
    }
    
    // --- Player Management ---

    private void handlePlayerChoice(int choice, Player player) {
        switch (choice) {
            case 1: // Create Session
                handleCreateSession();
                break;
            case 2: // Join Session
                handleJoinSession();
                break;
            case 3: // Search Trainers
                handleSearchTrainers();
                break;
            case 4: // View Messages
                handleViewMessages();
                break;
            case 5: // Logout
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
    
    // --- Trainer Management ---
    
    private void handleTrainerChoice(int choice, Trainer trainer) {
        switch (choice) {
            case 1: // View Bookings
            	handleViewBookingsForTrainer(trainer);
                break;
            case 2: // Update Profile
            	handleUpdateProfileForTrainer(trainer);
                break;
            case 3: // View Messages
                handleViewMessages();
                break;
            case 4: // Logout
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
            // Find the player's name using AuthManager
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
        
        // Logic to update the Trainer object
        if (!newName.isEmpty()) {
            // Assume you add setName(String name) method to User class
            trainer.setName(newName); 
        }
        
        if (!newRateStr.isEmpty()) {
            try {
                double newRate = Double.parseDouble(newRateStr);
                trainer.setHourlyRate(newRate); // Assume you add setHourlyRate(double rate) method to Trainer class
            } catch (NumberFormatException e) {
                System.err.println("Invalid rate format. Rate remains unchanged.");
            }
        }
        
        if (!newSpecialty.isEmpty()) {
            trainer.setSpecialty(newSpecialty); // Assume you add setSpecialty(String specialty) method to Trainer class
        }

        System.out.println("Profile updated successfully!");
    }
    
    // --- Shared Communication Logic ---

    private void handleViewMessages() {
        User user = authManager.getCurrentUser();
        if (user == null) return;
        
        communicationManager.getMessagesForCurrentUser().forEach(m -> System.out.println(m.toString()));
        
        String receiverPrefix = InputValidator.readString("Enter user ID prefix to send a new message (or '0' to return): ");
        
        if (!receiverPrefix.equals("0")) {
            Optional<User> receiverOptional = Database.getInstance().getAllUsers().stream()
                .filter(u -> u.getId().startsWith(receiverPrefix))
                .findFirst();
            
            if (receiverOptional.isPresent()) {
                String content = InputValidator.readString("Enter message content: ");
                communicationManager.sendMessage(receiverOptional.get().getId(), content);
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("User not found with that ID prefix.");
            }
        }
    }
    
    // --- Admin Dashboard (SystemManager) ---

    private void runAdminDashboard() {
        while (authManager.getCurrentUser() != null) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println(systemManager.displaySystemStatus());
            System.out.println("1. Approve Trainer Requests");
            System.out.println("2. Logout");
            
            int choice = InputValidator.readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    handleTrainerApproval();
                    break;
                case 2:
                    authManager.logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void handleTrainerApproval() {
        List<Trainer> pendingTrainers = Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .filter(t -> !t.isApproved())
                .collect(Collectors.toList());

        if (pendingTrainers.isEmpty()) {
            System.out.println("No pending trainer requests.");
            return;
        }

        System.out.println("\n--- Pending Trainers ---");
        pendingTrainers.forEach(t -> System.out.printf("[%s] %s (%s)\n", t.getId().substring(0, 4), t.getName(), t.getSpecialty()));
        
        String trainerIdPrefix = InputValidator.readString("Enter Trainer ID prefix to approve (or '0' to return): ");

        if (!trainerIdPrefix.equals("0")) {
            Trainer trainer = (Trainer) authManager.getUserById(trainerIdPrefix);
            if (trainer != null && trainer instanceof Trainer) {
                trainer.setApproved(true);
                System.out.println("Trainer " + trainer.getName() + " has been approved!");
            } else {
                System.out.println("Invalid ID or user is not a Trainer.");
            }
        }
    }
}