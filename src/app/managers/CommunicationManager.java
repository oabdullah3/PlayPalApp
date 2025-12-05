package app.managers;

import app.entities.Message;
import app.entities.Notification;
import app.entities.User;
import app.entities.UserMessage;
import app.patterns.command.Command;
import app.patterns.command.NotificationCommand;
import app.patterns.command.SendMessageCommand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommunicationManager {

    private final Database db = Database.getInstance();
    private final AuthManager authManager = AuthManager.getInstance();

    
    private static CommunicationManager instance;

    protected CommunicationManager() {}

    public static CommunicationManager getInstance() {
        if (instance == null) {
            instance = new CommunicationManager();
        }
        return instance;
    }
    
    public void send(Command command) {
        command.execute();
    }

    public void sendMessage(String receiverId, String content) {
        User sender = authManager.getCurrentUser();
        if (sender == null) {
            System.err.println("Error: Must be logged in to send a message.");
            return;
        }

        UserMessage message = new UserMessage(sender.getId(), receiverId, content);
        Command sendMessage = new SendMessageCommand(message);
        send(sendMessage);
    }
    

    public List<Message> getMessagesForCurrentUser() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        
        return db.findMessagesForUser(user.getId());
    }
    
    
    public void sendNotification(String receiverId, String content) {
		Notification notification = new Notification(receiverId, content);
		send(new NotificationCommand(notification));
    	
    }
}