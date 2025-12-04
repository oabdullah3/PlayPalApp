package app.main;

import app.entities.*;
import app.managers.*;
import app.utils.*;
import app.ui.AdminUI;
import app.ui.AuthUI;
import app.ui.PlayerUI;
import app.ui.TrainerUI;


import java.util.logging.Logger;
import java.util.logging.Level;

public class PlayPalApp {

	
    private final AuthManager authManager = AuthManager.getInstance();
    
    private final AuthUI authUI = new AuthUI();
    private final PlayerUI playerUI = new PlayerUI();
    private final TrainerUI trainerUI = new TrainerUI();
    private final AdminUI adminUI = new AdminUI();

    public static void main(String[] args) {
    	Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        System.out.println("--- Welcome to PlayPal CLI ---");
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
                        User loggedInUser = authUI.handleLogin();
                        if (loggedInUser != null) {
                            runDashboard(loggedInUser);
                        }
                        break;
                    case 2:
                        authUI.handleRegistration();
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

    
    private void runDashboard(User user) {
    	if (authManager.isAdmin(user)) {
    		adminUI.runAdminDashboard();
            return;
       }
        
        while (authManager.getCurrentUser() != null) {
            user.showMenuOptions();
            int choice = InputValidator.readInt("Enter choice: ");
            
            if (user instanceof Player) {
                playerUI.handlePlayerChoice(choice, (Player) user);
            } else if (user instanceof Trainer) {
            	trainerUI.handleTrainerChoice(choice, (Trainer) user);
            }
        }
    }
    
}