package app.entities;

import app.utils.PlayerState;

public class Player extends User {

    private static final double DEFAULT_PLAYER_BALANCE = 50.00;

    public Player(String name, String email, String password) {
        super(name, email, password, DEFAULT_PLAYER_BALANCE);
        this.state = new PlayerState(this); 
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Player Dashboard ---");
        this.state.showMenu();
    }
}