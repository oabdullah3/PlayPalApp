package app.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    
    private final String sessionId;
    private final String creatorId; // ID of the Player who created it
    private final String sport;
    private final String location;
    private final LocalDateTime time;
    private final int maxParticipants;
    private List<String> participantIds;

    public Session(String creatorId, String sport, String location, LocalDateTime time, int maxParticipants) {
        this.sessionId = UUID.randomUUID().toString();
        this.creatorId = creatorId;
        this.sport = sport;
        this.location = location;
        this.time = time;
        this.maxParticipants = maxParticipants;
        this.participantIds = new ArrayList<>();
        // Creator is automatically the first participant
        this.participantIds.add(creatorId);
    }
    
    public boolean isFull() {
        return participantIds.size() >= maxParticipants;
    }

    // --- Getters and Adders ---

    public String getCreatorId() {
        return creatorId;
    }
    
    public String getSessionId() {
        return sessionId;
    }

    public String getSport() {
        return sport;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void addParticipant(String userId) {
        this.participantIds.add(userId);
    }
    
    // For console display
    @Override
    public String toString() {
        return String.format("[%s] %s at %s (%s) - Players: %d/%d", 
            sessionId.substring(0, 4), sport, location, time.toLocalTime(), participantIds.size(), maxParticipants);
    }
}