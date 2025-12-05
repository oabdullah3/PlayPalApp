package app.entities;

import app.exceptions.InsufficientFundsException;

import java.util.UUID; // Used for unique IDs

public abstract class User {
    
    private String id;
    private String name;
    private final String email;
    private final String password;
    private double balance;

    public User(String name, String email, String password, double initialBalance) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = initialBalance;
    }
    
    public abstract void showMenuOptions();
    
    public void setId(String id) {
		this.id = id;
	}
    
    public void pay(double amount) throws InsufficientFundsException {
        if (this.balance < amount) {
            throw new InsufficientFundsException("Insufficient funds: " + this.balance);
        }
        this.balance -= amount;
    }
    
    public static User create(String name, String email, String password, boolean isTrainer, String specialty, double rate) {
        if (isTrainer) {
            return new Trainer(name, email, password, specialty, rate);
        } else {
            return new Player(name, email, password);
        }
    }

    public void receivePayment(double amount) {
        this.balance += amount;
    }
    
    
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
}