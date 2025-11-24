package app.utils;

import app.entities.Trainer;

public class TrainerState implements UserState {

    private final Trainer trainer;

    public TrainerState(Trainer trainer) {
        this.trainer = trainer;
    }

    @Override
    public void showMenu() {
        String verificationStatus = trainer.isApproved() ? "APPROVED" : "PENDING";
        
        System.out.printf("Welcome, Trainer %s! Status: %s. Earnings: $%.2f\n", 
            trainer.getName(), verificationStatus, trainer.getBalance());
        System.out.println("1. View Current Bookings");
        System.out.println("2. Update Profile/Rate ($" + trainer.getHourlyRate() + "/hr)");
        System.out.println("3. View Messages");
        System.out.println("4. Logout");
    }
}