package app.entities;

import app.utils.PlayerState;

public class Player extends User {

    // Default balance for new players for mock payment
    private static final double DEFAULT_PLAYER_BALANCE = 50.00;

    public Player(String name, String email, String password) {
        // Calls User constructor with a default balance
        super(name, email, password, DEFAULT_PLAYER_BALANCE);
        // Sets the concrete state implementation
        this.state = new PlayerState(this); 
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Player Dashboard ---");
        // Delegates to the PlayerState implementation
        this.state.showMenu();
    }
}