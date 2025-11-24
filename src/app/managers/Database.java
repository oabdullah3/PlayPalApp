package app.managers;

import app.entities.*;
import java.util.ArrayList;
import java.util.List;

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

    // Mock initial data for testing purposes
    private void initializeMockData() {
        allUsers.add(new Player("Admin User", "admin@playpal.com", "admin123")); 
        Trainer mockTrainer = new Trainer("Sarah Connor", "sarah@pal.com", "pword", "Yoga", 30.00);
        mockTrainer.setApproved(true);
        allUsers.add(mockTrainer);
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