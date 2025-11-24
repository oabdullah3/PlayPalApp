package app.patterns.command;

import app.entities.Message;
import app.managers.Database;

public class SendMessageCommand implements Command {
    
    private final Message message;
    private final Database db = Database.getInstance();

    public SendMessageCommand(Message message) {
        this.message = message;
    }

    @Override
    public void execute() {
        // Logic to store the message in the central database
        db.getAllMessages().add(this.message);
        System.out.println("Message command executed: Message from " 
                           + message.getSenderId().substring(0, 4) + " saved.");
    }
}