package app.entities;


public class UserMessage extends Message{

    private final String senderId;


    public UserMessage(String senderId, String receiverId, String content) {
        super(receiverId, content);
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    @Override
    public String toString() {
        return String.format("[%s] From %s: %s", 
            timestamp.toLocalTime(), senderId.substring(0, 4), content);
    }
}