package app.patterns.command;

public class NotificationCommand implements Command {

    private final String userId;
    private final String notificationContent;

    public NotificationCommand(String userId, String notificationContent) {
        this.userId = userId;
        this.notificationContent = notificationContent;
    }

    @Override
    public void execute() {
        // In a CLI, this executes by printing the alert directly to the console.
        System.out.println("\n[ALERT to " + userId.substring(0, 4) + "]: " 
                           + notificationContent);
    }
}