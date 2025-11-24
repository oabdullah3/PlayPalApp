package app.entities;

import app.utils.UserState;

import java.util.UUID; // Used for unique IDs

public abstract class User {
    
    private final String id;
    private String name;
    private final String email;
    private final String password; // Stored as plain text for simplicity
    private double balance;
    
    // State Pattern interface reference
    protected UserState state;

    public User(String name, String email, String password, double initialBalance) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = initialBalance;
        // The specific constructor (Player/Trainer) will set the concrete state implementation
    }
    
    // Abstract method for showing unique menu options
    public abstract void showMenuOptions();
    

    // --- Getters and Setters (Only necessary ones included) ---
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Example of a minimal state usage
    public void executeMenu() {
        if (state != null) {
            state.showMenu();
        }
    }
}