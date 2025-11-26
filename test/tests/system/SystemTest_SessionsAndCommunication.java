package tests.system;

import app.main.PlayPalApp;
import app.managers.Database;
import app.entities.*;
import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class SystemTest_SessionsAndCommunication {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
        Database.getInstance().getAllSessions().clear();
        Database.getInstance().getAllMessages().clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(System.out);
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(System.in));
    }

    private void prepareInput(String data) throws Exception {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(System.in));
    }

    @Test
    void testJoinSessionAndMessaging() throws Exception {
        // 1. Setup: Session exists
        Session s = new Session("creator", "Soccer", "Field", LocalDateTime.now(), 5);
        Database.getInstance().getAllSessions().add(s);
        String sId = s.getSessionId().substring(0, 4);
        
        // 2. Setup: Receiver for message
        Player p2 = new Player("Receiver", "r@r.com", "pw");
        Database.getInstance().getAllUsers().add(p2);
        String p2Id = p2.getId().substring(0, 4);

        String input = 
            // Login (User1)
            "2\nUser1\nu@u.com\npw\n1\n" +
            "1\nu@u.com\npw\n" +
            
            // Join Session
            "2\nSoccer\n" + sId + "\n" +
            
            // Send Message
            "4\n" + p2Id + "\nHello!\n" +
            
            // Logout
            "5\n3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("Successfully joined session"));
        assertTrue(output.contains("Message sent successfully"));
        
        // DB Verify
        assertEquals(2, s.getParticipantIds().size()); // Creator + User1
        assertEquals(1, Database.getInstance().getAllMessages().size());
    }
}