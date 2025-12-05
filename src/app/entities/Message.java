package app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Message {

    private String messageId;
    private final String receiverId;
    protected final String content;
    protected final LocalDateTime timestamp;
    private boolean isRead;


    public Message(String receiverId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }
    
    public void setId(String id) {
        this.messageId = id;
    }


    public String getMessageId() {
        return messageId;
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
    
    public abstract String toString();

}