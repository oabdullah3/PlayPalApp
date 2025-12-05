package app.managers;

import app.entities.Session;
import app.entities.User;
import app.exceptions.SessionFullException;
import app.exceptions.SessionNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SessionManager {

    private final Database db = Database.getInstance();
    private final AuthManager authManager = AuthManager.getInstance();

    // --- Singleton-like access ---
    private static SessionManager instance;

    protected SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(String sport, String location, LocalDateTime time, int maxParticipants) {
        User creator = authManager.getCurrentUser();
        if (creator == null) {
            System.err.println("Error: Must be logged in to create a session.");
            return;
        }

        Session newSession = new Session(creator.getId(), sport, location, time, maxParticipants);
        db.addSession(newSession);
        System.out.println("Session created successfully: " + newSession.getSessionId().substring(0, 4));
    }

    public List<Session> searchAvailableSessions(String searchSport) {
    	return db.findAvailableSessionsBySport(searchSport);
    }

    public void joinSession(String sessionId) throws SessionNotFoundException, SessionFullException {
        User user = authManager.getCurrentUser();
        if (user == null) {
            System.err.println("Error: Must be logged in to join a session.");
            return;
        }

        Session session = db.findSessionByIdPrefix(sessionId);
        
        if (session == null) {
            throw new SessionNotFoundException("Session not found with ID: " + sessionId);
        }

        if (session.isFull()) {
            throw new SessionFullException("Session is full. Max participants reached.");
        }
        
        if (session.getParticipantIds().contains(user.getId())) {
            System.out.println("You are already participating in this session.");
            return;
        }

        session.addParticipant(user.getId());
        db.addParticipantDirectly(session.getSessionId(), user.getId());
        System.out.println("Successfully joined session: " + session.getSport());

        String notificationContent = String.format("Session %s Update: %s has joined your session.", session.getSessionId().substring(0, 4), user.getName());
        CommunicationManager.getInstance().sendNotification(
            session.getCreatorId(), notificationContent);
    }

    public Session getSessionById(String sessionId) {
    	return db.findSessionByIdPrefix(sessionId);
    }
}