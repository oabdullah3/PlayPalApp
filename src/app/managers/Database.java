package app.managers;

import app.entities.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates; 
import org.bson.Document;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        String connectionString = "mongodb+srv://omerabdullah2984_db_user:RwHRN5z0IK90eogF@playpalapp.k1gpfaf.mongodb.net/?appName=PlayPalApp";
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("PlayPalDB");

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


    public void addUser(User user) {
        Document doc = new Document("_id", user.getId()) 
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

        usersCol.replaceOne(Filters.eq("_id", user.getId()), doc, new ReplaceOptions().upsert(true));
    }

    public void addSession(Session session) {
        String timeStr = getFieldViaReflection(session, "time").toString();
        Object max = getFieldViaReflection(session, "maxParticipants");
        
        String sId = session.getSessionId(); 

        Document doc = new Document("_id", sId)
                .append("creatorId", session.getCreatorId())
                .append("sport", session.getSport())
                .append("location", session.getLocation())
                .append("time", timeStr)
                .append("maxParticipants", max)
                .append("participantIds", session.getParticipantIds());

        sessionsCol.replaceOne(Filters.eq("_id", sId), doc, new ReplaceOptions().upsert(true));
    }
    

    public void updateSession(Session session) {
        addSession(session);
    }

    public void addBooking(Booking booking) {
        String bId = (String) getFieldViaReflection(booking, "bookingId"); 

        Document doc = new Document("_id", bId)
                .append("playerId", booking.getPlayerId())
                .append("trainerId", booking.getTrainerId())
                .append("amount", booking.getAmount());
        
        bookingsCol.replaceOne(Filters.eq("_id", bId), doc, new ReplaceOptions().upsert(true));
    }


	 public void addMessage(Message message) {
	     String mId = message.getMessageId(); 
	
	     Document doc = new Document("_id", mId)
	             .append("receiverId", message.getReceiverId())
	             .append("content", message.getContent())
	             .append("timestamp", message.getTimestamp().toString())
	             .append("isRead", message.isRead());
	
	     if (message instanceof UserMessage) {
	         doc.append("senderId", ((UserMessage) message).getSenderId());
	     } else if (message instanceof Notification) {
	         doc.append("senderId", "SYSTEM");
	     }
	
	     messagesCol.replaceOne(Filters.eq("_id", mId), doc, new ReplaceOptions().upsert(true));
	 }


    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        for (Document doc : usersCol.find()) {
            User u;
            String role = doc.getString("role");
            String realMongoId = doc.getString("_id");

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
            
            u.setId(realMongoId);
            u.setBalance(doc.getDouble("balance"));
            
            list.add(u);
        }
        return list;
    }

    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        for (Document doc : sessionsCol.find()) {
            LocalDateTime time = LocalDateTime.parse(doc.getString("time"));
            String realMongoId = doc.getString("_id");
            
            Session s = new Session(
                doc.getString("creatorId"),
                doc.getString("sport"),
                doc.getString("location"),
                time,
                doc.getInteger("maxParticipants")
            );

            s.setId(realMongoId);

            List<String> participants = doc.getList("participantIds", String.class);
            s.setParticipantIds(participants);

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
            
            forceSetField(b, "bookingId", doc.getString("_id"));
            
            list.add(b);
        }
        return list;
    }

    public List<Message> getAllMessages() {
        List<Message> list = new ArrayList<>();
        for (Document doc : messagesCol.find()) {
            UserMessage m = new UserMessage(
                doc.getString("senderId"),
                doc.getString("receiverId"),
                doc.getString("content")
            );
            
            forceSetField(m, "messageId", doc.getString("_id"));
            
            m.setRead(doc.getBoolean("isRead"));
            if (doc.containsKey("timestamp")) {
                forceSetField(m, "timestamp", LocalDateTime.parse(doc.getString("timestamp")));
            }

            list.add(m);
        }
        return list;
    }

    
    private void forceSetField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            Field field = null;
            while (clazz != null && field == null) {
                try { field = clazz.getDeclaredField(fieldName); } 
                catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
            }
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
            }
        } catch (Exception e) { }
    }

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

    
    public User findUserByEmail(String email) {
        Document doc = usersCol.find(Filters.eq("email", email)).first();
        if (doc == null) return null;
        
        User u;
        String realMongoId = doc.getString("_id"); // Get ID

        if ("TRAINER".equals(doc.getString("role"))) {
            u = new Trainer(doc.getString("name"), doc.getString("email"), doc.getString("password"),
                            doc.getString("specialty"), doc.getDouble("rate"));
            ((Trainer) u).setApproved(doc.getBoolean("isApproved", false));
        } else {
            u = new Player(doc.getString("name"), doc.getString("email"), doc.getString("password"));
        }
        u.setId(realMongoId); 
        
        u.setBalance(doc.getDouble("balance"));
        return u;
    }

    public User findUserByIdPrefix(String prefix) {
        Document doc = usersCol.find(Filters.regex("_id", "^" + prefix)).first();
        if (doc == null) return null;
        return findUserByEmail(doc.getString("email"));
    }
    
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
        List<Message> list = new ArrayList<>();

        for (Document doc : messagesCol.find(Filters.eq("receiverId", userId))) {
            String type = doc.getString("senderId").equals("SYSTEM") ? "NOTIFICATION" : "USER_MESSAGE";
            Message m;

            if ("NOTIFICATION".equals(type)) {
                m = new Notification(
                    doc.getString("receiverId"),
                    doc.getString("content")
                );
            } else {
                m = new UserMessage(
                    doc.getString("senderId"), 
                    doc.getString("receiverId"),
                    doc.getString("content")
                );
            }

            forceSetField(m, "messageId", doc.getString("_id"));
            
            m.setRead(doc.getBoolean("isRead"));
            if (doc.containsKey("timestamp")) {
                 forceSetField(m, "timestamp", LocalDateTime.parse(doc.getString("timestamp")));
            }

            list.add(m);
        }
        
        list.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
        
        return list;
    }
   

    public void updateTrainerApproval(String userId, boolean isApproved) {
        try {
            usersCol.updateOne(
                Filters.eq("_id", userId), 
                Updates.set("isApproved", isApproved)
            );
            System.out.println("DEBUG: Direct DB Update -> User " + userId + " isApproved=" + isApproved);
        } catch (Exception e) {
            System.err.println("DB ERROR: Could not update approval. " + e.getMessage());
        }
    }

    public void addParticipantDirectly(String sessionId, String playerId) {
        try {
            sessionsCol.updateOne(
                Filters.eq("_id", sessionId),
                Updates.push("participantIds", playerId)
            );
            System.out.println("DEBUG: Direct DB Update -> Added " + playerId + " to Session " + sessionId);
        } catch (Exception e) {
            System.err.println("DB ERROR: Could not add participant. " + e.getMessage());
        }
    }
}