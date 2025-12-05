package app.entities;

public class Notification extends Message {

    public Notification(String receiverId, String content) {
        super(receiverId, content);
    }

    @Override
    public String toString() {
        return String.format("[%s] From %s: %s", 
            timestamp.toLocalTime(), "[System]", content);
    }
}