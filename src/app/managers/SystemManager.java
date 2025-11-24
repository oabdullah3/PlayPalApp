package app.managers;

import app.entities.User;
import app.entities.Trainer;

// The SystemManager class must also be a Singleton for centralized control.
public class SystemManager {

    // --- Singleton Implementation ---
    private static SystemManager instance;
    private final Database db;

    private SystemManager() {
        this.db = Database.getInstance();
    }

    public static SystemManager getInstance() {
        if (instance == null) {
            instance = new SystemManager();
        }
        return instance;
    }
    // --------------------------------

    // --- Admin Functions (Skeleton) ---
    
    // Mock function to approve a trainer (used by admin CLI)
    public void approveTrainer(String trainerId) {
        // In a real app, this finds the trainer object in the DB and calls setApproved(true)
        System.out.println("System: Trainer " + trainerId.substring(0, 4) + " approved successfully.");
    }

    // Simple status report
    public void displaySystemStatus() {
        System.out.println("\n--- System Status Report ---");
        System.out.println("Total Users: " + db.getAllUsers().size());
        System.out.println("Total Sessions: " + db.getAllSessions().size());
        System.out.println("--------------------------");
    }
}