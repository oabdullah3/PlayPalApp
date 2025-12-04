package app.ui;

import java.util.Optional;

import app.entities.User;
import app.managers.AuthManager;
import app.managers.CommunicationManager;
import app.managers.Database;
import app.utils.InputValidator;

public class SharedUI {
	
	private final CommunicationManager communicationManager = CommunicationManager.getInstance();
	private final AuthManager authManager = AuthManager.getInstance();
	
	public void handleViewMessages() {
        User user = authManager.getCurrentUser();
        if (user == null) return;
        
        communicationManager.getMessagesForCurrentUser().forEach(m -> System.out.println(m.toString()));
        
        String receiverPrefix = InputValidator.readString("Enter user ID prefix to send a new message (or '0' to return): ");
        
        if (!receiverPrefix.equals("0")) {
            Optional<User> receiverOptional = Database.getInstance().getAllUsers().stream()
                .filter(u -> u.getId().startsWith(receiverPrefix))
                .findFirst();
            
            if (receiverOptional.isPresent()) {
                String content = InputValidator.readString("Enter message content: ");
                communicationManager.sendMessage(receiverOptional.get().getId(), content);
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("User not found with that ID prefix.");
            }
        }
    }
	
}