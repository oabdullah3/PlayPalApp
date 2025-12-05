package app.patterns.command;

import app.entities.UserMessage;
import app.managers.Database;

public class SendMessageCommand implements Command {
    
    private final UserMessage message;
    private final Database db = Database.getInstance();

    public SendMessageCommand(UserMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
    	db.addMessage(this.message);
        System.out.println("Message command executed: Message from " 
                           + message.getSenderId().substring(0, 4) + " saved.");
    }
}