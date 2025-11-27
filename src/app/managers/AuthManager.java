package app.managers;

import app.entities.*;
import app.exceptions.*;

public class AuthManager {

    private final Database db = Database.getInstance();
    private User currentUser = null;

    private static AuthManager instance;

    protected AuthManager() {}

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }
    
    public User login(String email, String password) throws UserNotFoundException, InvalidCredentialsException {
    	User user = db.findUserByEmail(email);

        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid password for user: " + email);
        }

        this.currentUser = user;
        System.out.println("Login successful for " + user.getName() + ".");
        return user;
    }

    public void register(String name, String email, String password, boolean isTrainer, String specialty, double rate) throws DuplicateEmailException {

    	if (db.emailExists(email)) {
            throw new DuplicateEmailException("Email already in use: " + email);
        }

        User newUser = User.create(name, email, password, isTrainer, specialty, rate);

        db.addUser(newUser);
        System.out.println("Registration successful. Account created as " + (isTrainer ? "Trainer (Pending Approval)" : "Player") + ".");
    }
    
    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
        System.out.println("Logout successful.");
    }

    public User getUserById(String userId) {
    	return db.findUserByIdPrefix(userId);
    }
    
    private static final String ADMIN_EMAIL = "admin@playpal.com";

    public boolean isAdmin(User user) {
        return user != null && user.getEmail().equalsIgnoreCase(ADMIN_EMAIL);
    }
}