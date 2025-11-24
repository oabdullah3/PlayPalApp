package app.managers;

import app.entities.Message;
import app.entities.User;
import app.patterns.command.Command;
import app.patterns.command.NotificationCommand;
import app.patterns.command.SendMessageCommand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommunicationManager {

    private final Database db = Database.getInstance();
    private final AuthManager authManager = AuthManager.getInstance();

    // --- Singleton-like access ---
    private static CommunicationManager instance;

    private CommunicationManager() {}

    public static CommunicationManager getInstance() {
        if (instance == null) {
            instance = new CommunicationManager();
        }
        return instance;
    }
    // -----------------------------

    // --- Core Methods ---
    
    // Executes any Command (SendMessageCommand, NotificationCommand)
    public void send(Command command) {
        // For CLI simplicity, we execute commands immediately
        command.execute();
    }

    public void sendMessage(String receiverId, String content) {
        User sender = authManager.getCurrentUser();
        if (sender == null) {
            System.err.println("Error: Must be logged in to send a message.");
            return;
        }

        // 1. Create the message object
        Message message = new Message(sender.getId(), receiverId, content);
        
        // 2. Execute the command to save the message
        Command sendMessage = new SendMessageCommand(message);
        send(sendMessage);
    }

    public List<Message> getMessagesForCurrentUser() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        
        return db.getAllMessages().stream()
                .filter(m -> m.getReceiverId().equals(user.getId()))
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    // --- Helper Methods to send system notifications ---

    public void sendSessionUpdateNotification(String userId, String sessionId, String content) {
        String notification = String.format("Session %s Update: %s", sessionId.substring(0, 4), content);
        Command cmd = new NotificationCommand(userId, notification);
        send(cmd);
    }

    public void sendBookingNotification(String playerId, String trainerId, double cost) {
        String playerNotification = String.format("Booking confirmed! You paid $%.2f to Trainer %s.", cost, authManager.getUserById(trainerId).getName());
        String trainerNotification = String.format("New Booking received from Player %s! You earned $%.2f.", authManager.getUserById(playerId).getName(), cost);

        // Notify both parties
        send(new NotificationCommand(playerId, playerNotification));
        send(new NotificationCommand(trainerId, trainerNotification));
    }
}