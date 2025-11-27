package app.managers;

import app.entities.User;
import app.entities.Trainer;

// The SystemManager class must also be a Singleton for centralized control.
public class SystemManager {

    // --- Singleton Implementation ---
    private static SystemManager instance;
    private final Database db;

    protected SystemManager() {
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
    	User user = db.findUserByIdPrefix(trainerId);

        if (user != null && user instanceof Trainer) {
            ((Trainer) user).setApproved(true);
            System.out.println("System: Trainer " + user.getName() + " approved successfully.");
        } else {
            System.out.println("System: Trainer ID not found or user is not a Trainer.");
        }
    }
    
    public String displaySystemStatus() {
        StringBuilder report = new StringBuilder();
        report.append("\n--- System Status Report ---\n");
        report.append("Total Users: ").append(db.getAllUsers().size()).append("\n");
        report.append("Total Sessions: ").append(db.getAllSessions().size()).append("\n");
        report.append("--------------------------");
        return report.toString();
    }
}