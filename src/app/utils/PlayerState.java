package app.utils;

import app.entities.Player;

public class PlayerState implements UserState {

    private final Player player;

    public PlayerState(Player player) {
        this.player = player;
    }

    @Override
    public void showMenu() {
        System.out.printf("Welcome, Player %s! Current Balance: $%.2f\n", 
            player.getName(), player.getBalance());
        System.out.println("1. Create Session");
        System.out.println("2. Join Session");
        System.out.println("3. Search Trainers");
        System.out.println("4. View Messages");
        System.out.println("5. Logout");
    }
}