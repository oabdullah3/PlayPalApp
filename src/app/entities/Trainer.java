package app.entities;

import app.ui.PlayerUI;
import app.ui.TrainerUI;

public class Trainer extends User {
    
    private String specialty;
    private double hourlyRate;
    private boolean isApproved = false;
    
    public Trainer(String name, String email, String password, String specialty, double hourlyRate) {
        super(name, email, password, 0.00); 
        this.specialty = specialty;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Trainer Dashboard ---");
        String verificationStatus = isApproved() ? "APPROVED" : "PENDING";
        System.out.printf("Welcome, Trainer %s! Status: %s. Earnings: $%.2f\n", 
            getName(), verificationStatus, getBalance());
        System.out.println("1. View Current Bookings");
        System.out.println("2. Update Profile/Rate ($" + getHourlyRate() + "/hr)");
        System.out.println("3. View Messages");
        System.out.println("4. Logout");
    }
    
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getSpecialty() {
        return specialty;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}