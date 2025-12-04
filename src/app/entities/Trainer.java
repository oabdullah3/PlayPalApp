package app.entities;

import app.utils.TrainerState;

public class Trainer extends User {
    
    private String specialty;
    private double hourlyRate;
    private boolean isApproved = false;
    
    public Trainer(String name, String email, String password, String specialty, double hourlyRate) {
        super(name, email, password, 0.00); 
        this.specialty = specialty;
        this.hourlyRate = hourlyRate;
        this.state = new TrainerState(this);
    }

    @Override
    public void showMenuOptions() {
        System.out.println("\n--- Trainer Dashboard ---");
        this.state.showMenu();
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