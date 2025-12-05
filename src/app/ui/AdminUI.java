package app.ui;

import java.util.List;
import java.util.stream.Collectors;

import app.entities.Trainer;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SystemManager;
import app.utils.InputValidator;

public class AdminUI {
	
	private final SystemManager systemManager = SystemManager.getInstance();
	private final AuthManager authManager = AuthManager.getInstance();
	public void runAdminDashboard() {
        while (authManager.getCurrentUser() != null) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println(systemManager.displaySystemStatus());
            System.out.println("1. Approve Trainer Requests");
            System.out.println("2. Logout");
            
            int choice = InputValidator.readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    handleTrainerApproval();
                    break;
                case 2:
                    authManager.logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void handleTrainerApproval() {
        List<Trainer> pendingTrainers = Database.getInstance().findPendingTrainers();
        if (pendingTrainers.isEmpty()) {
            System.out.println("No pending trainer requests.");
            return;
        }

        System.out.println("\n--- Pending Trainers ---");
        pendingTrainers.forEach(t -> System.out.printf("[%s] %s (%s)\n", t.getId().substring(0, 4), t.getName(), t.getSpecialty()));
        
        String trainerIdPrefix = InputValidator.readString("Enter Trainer ID prefix to approve (or '0' to return): ");

        if (!trainerIdPrefix.equals("0")) {
            Trainer trainer = (Trainer) authManager.getUserById(trainerIdPrefix);
            systemManager.approveTrainer(trainer.getId());
        }
    }
}