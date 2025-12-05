package app.patterns.command;

import app.entities.Notification;
import app.managers.Database;

public class NotificationCommand implements Command {

	private final Notification notification;
    private final Database db = Database.getInstance();

    public NotificationCommand(Notification notification) {
        this.notification = notification;
    }

    @Override
    public void execute() {
        db.addMessage(this.notification);
        System.out.println("Message command executed: Message from " 
                + "[SYSTEM]" + " saved."); }
}