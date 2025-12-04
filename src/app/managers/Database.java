package app.managers;

import app.entities.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Database {

    private static Database instance;
    
    private Database() {
        initializeMockData();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
    
    private final List<User> allUsers = new ArrayList<>();
    private final List<Session> allSessions = new ArrayList<>();
    private final List<Booking> allBookings = new ArrayList<>();
    private final List<Message> allMessages = new ArrayList<>();

    
    private void initializeMockData() {
        allUsers.add(new Player("Admin User", "admin@playpal.com", "admin123")); 
        Trainer mockTrainer = new Trainer("Sarah Connor", "sarah@pal.com", "pword", "Yoga", 30.00);
        mockTrainer.setApproved(true);
        allUsers.add(mockTrainer);
    }
    
 
    public List<Trainer> findPendingTrainers() {
        return allUsers.stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .filter(t -> !t.isApproved())
                .collect(Collectors.toList());
    }
    
	
	 public User findUserByEmail(String email) {
	     return allUsers.stream()
	             .filter(u -> u.getEmail().equalsIgnoreCase(email))
	             .findFirst()
	             .orElse(null);
	 }
	
	 public User findUserByIdPrefix(String prefix) {
	     return allUsers.stream()
	             .filter(u -> u.getId().startsWith(prefix))
	             .findFirst()
	             .orElse(null);
	 }
	
	 
	 public boolean emailExists(String email) {
	     return allUsers.stream()
	             .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
	 }
 

	public void addUser(User user) {
	  allUsers.add(user);
	}
	
	public void addSession(Session session) {
	  allSessions.add(session);
	}
	
	public void addBooking(Booking booking) {
	  allBookings.add(booking);
	}
	
	public void addMessage(Message message) {
	  allMessages.add(message);
	}
	
	public List<Trainer> findApprovedTrainersBySpecialty(String specialty) {
	    return allUsers.stream()
	            .filter(u -> u instanceof Trainer)
	            .map(u -> (Trainer) u)
	            .filter(t -> t.isApproved() && t.getSpecialty().equalsIgnoreCase(specialty))
	            .collect(Collectors.toList());
	}

	public List<Booking> findBookingsByTrainerId(String trainerId) {
	    return allBookings.stream()
	            .filter(b -> b.getTrainerId().equals(trainerId))
	            .collect(Collectors.toList());
	}
	
	public List<Session> findAvailableSessionsBySport(String sport) {
	    return allSessions.stream()
	            .filter(s -> s.getSport().equalsIgnoreCase(sport) && !s.isFull())
	            .collect(Collectors.toList());
	}

	public Session findSessionByIdPrefix(String prefix) {
	    return allSessions.stream()
	            .filter(s -> s.getSessionId().startsWith(prefix))
	            .findFirst()
	            .orElse(null);
	}
	
	public List<Message> findMessagesForUser(String userId) {
	    return allMessages.stream()
	            .filter(m -> m.getReceiverId().equals(userId))
	            .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
	            .collect(Collectors.toList());
	}


    public List<User> getAllUsers() {
        return allUsers;
    }

    public List<Session> getAllSessions() {
        return allSessions;
    }

    public List<Booking> getAllBookings() {
        return allBookings;
    }
    
    public List<Message> getAllMessages() {
        return allMessages;
    }
}