package app.managers;

import app.entities.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Database {

    private static Database instance;
    private final MongoDatabase database;
    
    // Collections
    private final MongoCollection<Document> usersCol;
    private final MongoCollection<Document> sessionsCol;
    private final MongoCollection<Document> bookingsCol;
    private final MongoCollection<Document> messagesCol;

    private Database() {
        // 1. CONNECT TO MONGODB (Replace with your actual Connection String)
        String connectionString = "mongodb+srv://omerabdullah2984_db_user:RwHRN5z0IK90eogF@playpalapp.k1gpfaf.mongodb.net/?appName=PlayPalApp";
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("PlayPalDB");

        // 2. GET COLLECTIONS
        this.usersCol = database.getCollection("users");
        this.sessionsCol = database.getCollection("sessions");
        this.bookingsCol = database.getCollection("bookings");
        this.messagesCol = database.getCollection("messages");
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // =========================================================================
    //   THE MAGIC: REFLECTION HELPER
    //   Allows us to edit 'final' private fields without changing Entity classes
    // =========================================================================
    private void forceSetField(Object target, String fieldName, Object value) {
        try {
            // Traverse up to the parent class (User) if field is not in Child (Trainer/Player)
            Class<?> clazz = target.getClass();
            Field field = null;
            
            while (clazz != null && field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass(); // Check parent
                }
            }
            
            if (field != null) {
                field.setAccessible(true); // BREAK PRIVATE ACCESS
                field.set(target, value);
            }
        } catch (Exception e) {
            System.err.println("Reflection Error on field '" + fieldName + "': " + e.getMessage());
        }
    }

    // =========================================================================
    //   WRITE METHODS (Save to MongoDB)
    // =========================================================================

    public void addUser(User user) {
        Document doc = new Document("_id", user.getId()) // Use _id for MongoDB Primary Key
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("balance", user.getBalance());

        if (user instanceof Trainer) {
            Trainer t = (Trainer) user;
            doc.append("role", "TRAINER")
               .append("specialty", t.getSpecialty())
               .append("rate", t.getHourlyRate())
               .append("isApproved", t.isApproved());
        } else {
            doc.append("role", "PLAYER");
        }

        // Upsert (Insert or Replace if exists)
        usersCol.replaceOne(Filters.eq("_id", user.getId()), doc, new ReplaceOptions().upsert(true));
    }

    public void addSession(Session session) {
        Document doc = new Document("_id", session.getSessionId())
                .append("creatorId", session.getCreatorId())
                .append("sport", session.getSport())
                .append("location", session.getLocation())
                .append("time", session.toString()) // Storing toString() contains the ISO date
                // Note: Better to store session.getTime().toString() if public, but reflection handles 'toString'
                .append("maxParticipants", session.toString().split("/")[1]) // parsing from toString if getter unavailable
                // Or better, let's just assume we can get these values. 
                // Since I can't see getters in your *latest* Session.java snippet, 
                // I will use Reflection to GET them safely too.
                .append("participantIds", session.getParticipantIds());
        
        // RE-DOING fields safely with reflection getters just in case
        doc.put("maxParticipants", getFieldViaReflection(session, "maxParticipants"));
        doc.put("time", getFieldViaReflection(session, "time").toString());

        sessionsCol.replaceOne(Filters.eq("_id", session.getSessionId()), doc, new ReplaceOptions().upsert(true));
    }
    
    // Need this to update participants list
    public void updateSession(Session session) {
        addSession(session); // MongoDB replaceOne handles updates perfectly
    }

    public void addBooking(Booking booking) {
        Document doc = new Document("_id", booking.getBookingId())
                .append("playerId", booking.getPlayerId())
                .append("trainerId", booking.getTrainerId())
                .append("amount", booking.getAmount());
                // Add status/timestamp if your Booking class has them
        
        bookingsCol.replaceOne(Filters.eq("_id", booking.getBookingId()), doc, new ReplaceOptions().upsert(true));
    }

    public void addMessage(Message message) {
        Document doc = new Document("_id", message.getMessageId())
                .append("senderId", message.getSenderId())
                .append("receiverId", message.getReceiverId())
                .append("content", message.getContent())
                .append("timestamp", message.getTimestamp().toString())
                .append("isRead", message.isRead());

        messagesCol.replaceOne(Filters.eq("_id", message.getMessageId()), doc, new ReplaceOptions().upsert(true));
    }

    // =========================================================================
    //   READ METHODS (Load from MongoDB + Fix IDs)
    // =========================================================================

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        for (Document doc : usersCol.find()) {
            User u;
            String role = doc.getString("role");
            String id = doc.getString("_id"); // THE REAL ID

            if ("TRAINER".equals(role)) {
                u = new Trainer(
                    doc.getString("name"), doc.getString("email"), doc.getString("password"),
                    doc.getString("specialty"), doc.getDouble("rate")
                );
                ((Trainer) u).setApproved(doc.getBoolean("isApproved", false));
            } else {
                u = new Player(
                    doc.getString("name"), doc.getString("email"), doc.getString("password")
                );
            }
            
            // 1. FIX THE ID (Overwrite the random UUID generated by constructor)
            forceSetField(u, "id", id);
            
            // 2. Restore Balance
            forceSetField(u, "balance", doc.getDouble("balance"));
            
            list.add(u);
        }
        return list;
    }

    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        for (Document doc : sessionsCol.find()) {
            // Parse time
            LocalDateTime time = LocalDateTime.parse(doc.getString("time"));
            
            Session s = new Session(
                doc.getString("creatorId"),
                doc.getString("sport"),
                doc.getString("location"),
                time,
                doc.getInteger("maxParticipants")
            );

            // 1. FIX THE ID
            forceSetField(s, "sessionId", doc.getString("_id"));

            // 2. RESTORE PARTICIPANTS
            List<String> participants = doc.getList("participantIds", String.class);
            forceSetField(s, "participantIds", participants);

            list.add(s);
        }
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        for (Document doc : bookingsCol.find()) {
            Booking b = new Booking(
                doc.getString("playerId"),
                doc.getString("trainerId"),
                doc.getDouble("amount")
            );
            
            // 1. FIX THE ID
            forceSetField(b, "bookingId", doc.getString("_id"));
            
            list.add(b);
        }
        return list;
    }

    public List<Message> getAllMessages() {
        List<Message> list = new ArrayList<>();
        for (Document doc : messagesCol.find()) {
            Message m = new Message(
                doc.getString("senderId"),
                doc.getString("receiverId"),
                doc.getString("content")
            );
            
            // 1. FIX THE ID
            forceSetField(m, "messageId", doc.getString("_id"));
            
            // 2. Restore Read Status & Time (if needed)
            m.setRead(doc.getBoolean("isRead"));
            
            // Force timestamp overwrite if needed
            if (doc.containsKey("timestamp")) {
                forceSetField(m, "timestamp", LocalDateTime.parse(doc.getString("timestamp")));
            }

            list.add(m);
        }
        return list;
    }

    // =========================================================================
    //   HELPER: Get Private Field (For saving data)
    // =========================================================================
    private Object getFieldViaReflection(Object target, String fieldName) {
        try {
            Class<?> clazz = target.getClass();
            Field field = null;
            while (clazz != null && field == null) {
                try { field = clazz.getDeclaredField(fieldName); } 
                catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
            }
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =========================================================================
    //   EXISTING HELPER METHODS (Bridge to Manager Classes)
    //   These ensure your other Managers don't break.
    // =========================================================================
    
    public User findUserByEmail(String email) {
        // Optimization: Query Mongo directly instead of streaming all users
        Document doc = usersCol.find(Filters.eq("email", email)).first();
        if (doc == null) return null;
        
        // Reconstruct just this one user
        // (Copied logic from getAllUsers logic for single object)
        User u;
        if ("TRAINER".equals(doc.getString("role"))) {
            u = new Trainer(doc.getString("name"), doc.getString("email"), doc.getString("password"),
                            doc.getString("specialty"), doc.getDouble("rate"));
            ((Trainer) u).setApproved(doc.getBoolean("isApproved"));
        } else {
            u = new Player(doc.getString("name"), doc.getString("email"), doc.getString("password"));
        }
        forceSetField(u, "id", doc.getString("_id"));
        forceSetField(u, "balance", doc.getDouble("balance"));
        return u;
    }

    public User findUserByIdPrefix(String prefix) {
        // Regex search for ID prefix
        Document doc = usersCol.find(Filters.regex("_id", "^" + prefix)).first();
        if (doc == null) return null;
        return findUserByEmail(doc.getString("email")); // Reuse reconstruction logic
    }
    
    // Keep the other filters as streams or convert to Mongo Queries as needed.
    // For "Don't touch existing code", streaming the lists returned above is fine, 
    // but less efficient than direct queries.
    
    public List<Trainer> findPendingTrainers() {
        return getAllUsers().stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .filter(t -> !t.isApproved())
                .collect(Collectors.toList());
    }

    public boolean emailExists(String email) {
        return usersCol.countDocuments(Filters.eq("email", email)) > 0;
    }
    
    public List<Trainer> findApprovedTrainersBySpecialty(String specialty) {
        return getAllUsers().stream()
                .filter(u -> u instanceof Trainer)
                .map(u -> (Trainer) u)
                .filter(t -> t.isApproved() && t.getSpecialty().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }

    public List<Booking> findBookingsByTrainerId(String trainerId) {
        return getAllBookings().stream()
                .filter(b -> b.getTrainerId().equals(trainerId))
                .collect(Collectors.toList());
    }
    
    public List<Session> findAvailableSessionsBySport(String sport) {
        return getAllSessions().stream()
                .filter(s -> s.getSport().equalsIgnoreCase(sport) && !s.isFull())
                .collect(Collectors.toList());
    }

    public Session findSessionByIdPrefix(String prefix) {
        return getAllSessions().stream()
                .filter(s -> s.getSessionId().startsWith(prefix))
                .findFirst()
                .orElse(null);
    }
    
    public List<Message> findMessagesForUser(String userId) {
        return getAllMessages().stream()
                .filter(m -> m.getReceiverId().equals(userId))
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .collect(Collectors.toList());
    }
}



//package app.managers;
//
//import app.entities.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class DatabaseManager {
//
//    private static DatabaseManager instance;
//    
//    private DatabaseManager() {
//        initializeMockData();
//    }
//
//    public static DatabaseManager getInstance() {
//        if (instance == null) {
//            instance = new DatabaseManager();
//        }
//        return instance;
//    }
//    
//    private final List<User> allUsers = new ArrayList<>();
//    private final List<Session> allSessions = new ArrayList<>();
//    private final List<Booking> allBookings = new ArrayList<>();
//    private final List<Message> allMessages = new ArrayList<>();
//
//    
//    private void initializeMockData() {
//        allUsers.add(new Player("Admin User", "admin@playpal.com", "admin123")); 
//        Trainer mockTrainer = new Trainer("Sarah Connor", "sarah@pal.com", "pword", "Yoga", 30.00);
//        mockTrainer.setApproved(true);
//        allUsers.add(mockTrainer);
//    }
//    
// 
//    public List<Trainer> findPendingTrainers() {
//        return allUsers.stream()
//                .filter(u -> u instanceof Trainer)
//                .map(u -> (Trainer) u)
//                .filter(t -> !t.isApproved())
//                .collect(Collectors.toList());
//    }
//    
//	
//	 public User findUserByEmail(String email) {
//	     return allUsers.stream()
//	             .filter(u -> u.getEmail().equalsIgnoreCase(email))
//	             .findFirst()
//	             .orElse(null);
//	 }
//	
//	 public User findUserByIdPrefix(String prefix) {
//	     return allUsers.stream()
//	             .filter(u -> u.getId().startsWith(prefix))
//	             .findFirst()
//	             .orElse(null);
//	 }
//	
//	 
//	 public boolean emailExists(String email) {
//	     return allUsers.stream()
//	             .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
//	 }
// 
//
//	public void addUser(User user) {
//	  allUsers.add(user);
//	}
//	
//	public void addSession(Session session) {
//	  allSessions.add(session);
//	}
//	
//	public void addBooking(Booking booking) {
//	  allBookings.add(booking);
//	}
//	
//	public void addMessage(Message message) {
//	  allMessages.add(message);
//	}
//	
//	public List<Trainer> findApprovedTrainersBySpecialty(String specialty) {
//	    return allUsers.stream()
//	            .filter(u -> u instanceof Trainer)
//	            .map(u -> (Trainer) u)
//	            .filter(t -> t.isApproved() && t.getSpecialty().equalsIgnoreCase(specialty))
//	            .collect(Collectors.toList());
//	}
//
//	public List<Booking> findBookingsByTrainerId(String trainerId) {
//	    return allBookings.stream()
//	            .filter(b -> b.getTrainerId().equals(trainerId))
//	            .collect(Collectors.toList());
//	}
//	
//	public List<Session> findAvailableSessionsBySport(String sport) {
//	    return allSessions.stream()
//	            .filter(s -> s.getSport().equalsIgnoreCase(sport) && !s.isFull())
//	            .collect(Collectors.toList());
//	}
//
//	public Session findSessionByIdPrefix(String prefix) {
//	    return allSessions.stream()
//	            .filter(s -> s.getSessionId().startsWith(prefix))
//	            .findFirst()
//	            .orElse(null);
//	}
//	
//	public List<Message> findMessagesForUser(String userId) {
//	    return allMessages.stream()
//	            .filter(m -> m.getReceiverId().equals(userId))
//	            .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
//	            .collect(Collectors.toList());
//	}
//
//
//    public List<User> getAllUsers() {
//        return allUsers;
//    }
//
//    public List<Session> getAllSessions() {
//        return allSessions;
//    }
//
//    public List<Booking> getAllBookings() {
//        return allBookings;
//    }
//    
//    public List<Message> getAllMessages() {
//        return allMessages;
//    }
//}