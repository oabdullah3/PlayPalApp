package app.entities;

import app.ui.PlayerUI;
import app.ui.TrainerUI;

public class Player extends User {

    private static final double DEFAULT_PLAYER_BALANCE = 50.00;

    public Player(String name, String email, String password) {
        super(name, email, password, DEFAULT_PLAYER_BALANCE);
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Player Dashboard ---");
        System.out.printf("Welcome, Player %s! Current Balance: $%.2f\n", getName(), getBalance());
        System.out.println("1. Create Session");
        System.out.println("2. Join Session");
        System.out.println("3. Search Trainers");
        System.out.println("4. View Messages");
        System.out.println("5. Logout");
    }
    
}