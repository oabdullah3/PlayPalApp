package app.ui;

import app.entities.User;
import app.exceptions.DuplicateEmailException;
import app.exceptions.InvalidCredentialsException;
import app.exceptions.UserNotFoundException;
import app.managers.AuthManager;
import app.utils.InputValidator;

public class AuthUI {
	private final AuthManager authManager = AuthManager.getInstance();
	
	public User handleLogin() {
        String email = InputValidator.readString("Enter email: ");
        String password = InputValidator.readString("Enter password: ");
        try {
            return authManager.login(email, password);
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            System.err.println("Login Failed: " + e.getMessage());
            return null;
        }
    }

    public void handleRegistration() {
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
}