package app.managers;

import app.entities.*;
import app.exceptions.*;
import app.utils.PlayerState;
import app.utils.TrainerState;

import java.util.Optional;
import java.util.List;

public class AuthManager {

    private final Database db = Database.getInstance();
    private User currentUser = null; // Tracks the currently logged-in user

    // --- Singleton-like access (can be non-singleton, but useful for CLI) ---
    private static AuthManager instance;

    private AuthManager() {}

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }
    
    public User login(String email, String password) throws UserNotFoundException, InvalidCredentialsException {
        Optional<User> userOptional = db.getAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();

        // Check password
        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid password for user: " + email);
        }

        this.currentUser = user;
        System.out.println("Login successful for " + user.getName() + ".");
        return user;
    }

    public void register(String name, String email, String password, boolean isTrainer, String specialty, double rate) throws DuplicateEmailException {
        // Check for duplicate email
        boolean emailExists = db.getAllUsers().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new DuplicateEmailException("Email already in use: " + email);
        }

        User newUser;
        if (isTrainer) {
            newUser = new Trainer(name, email, password, specialty, rate);
        } else {
            newUser = new Player(name, email, password);
        }

        db.getAllUsers().add(newUser);
        System.out.println("Registration successful. Account created as " + (isTrainer ? "Trainer (Pending Approval)" : "Player") + ".");
    }
    
    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
        System.out.println("Logout successful.");
    }

    // --- Utility Method ---
    public User getUserById(String userId) {
        return db.getAllUsers().stream()
        		// 2. Filter the stream to find an ID that starts with the given prefix
                .filter(u -> u.getId().startsWith(userId)) 
                // 3. Take the first match
                .findFirst()
                // 4. Return null if no match is found
                .orElse(null);
    }
}