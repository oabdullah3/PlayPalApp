package app.entities;

import app.utils.TrainerState;

public class Trainer extends User {
    
    private String specialty;
    private double hourlyRate;
    private boolean isApproved = false; // Requires SystemManager approval
    
    public Trainer(String name, String email, String password, String specialty, double hourlyRate) {
        // Trainers start with zero balance; they earn money.
        super(name, email, password, 0.00); 
        this.specialty = specialty;
        this.hourlyRate = hourlyRate;
        // Sets the concrete state implementation
        this.state = new TrainerState(this);
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Trainer Dashboard ---");
        // Delegates to the TrainerState implementation
        this.state.showMenu();
    }
    
    // --- Getters and Setters ---
    
 // Add to Trainer.java (Concrete Class)

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

    // Used by SystemManager to verify the trainer
    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}