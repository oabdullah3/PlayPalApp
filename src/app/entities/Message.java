package app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {

    private final String messageId;
    private final String senderId;
    private final String receiverId;
    private final String content;
    private final LocalDateTime timestamp;
    private boolean isRead;


    public Message(String senderId, String receiverId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }


    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }


    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return String.format("[%s] From %s: %s", 
            timestamp.toLocalTime(), senderId.substring(0, 4), content);
    }
}