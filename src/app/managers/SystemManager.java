package app.managers;

import app.entities.User;
import app.entities.Trainer;
import java.util.List;

// The SystemManager class must also be a Singleton for centralized control.
public class SystemManager {

    
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
    
    public void approveTrainer(String trainerId) {
    	User user = db.findUserByIdPrefix(trainerId);

        if (user != null && user instanceof Trainer) {
            ((Trainer) user).setApproved(true);
            System.out.println("System: Trainer " + user.getName() + " approved successfully.");
        } else {
            System.out.println("System: Trainer ID not found or user is not a Trainer.");
        }
    }
    
    public List<Trainer> getPendingTrainers() {
        return db.findPendingTrainers();
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