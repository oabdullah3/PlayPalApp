package tests.system.all;

import app.main.PlayPalApp;
import app.managers.Database;
import app.entities.*;
import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class SystemTest_PlayerCompleteJourney {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
        Database.getInstance().getAllSessions().clear();
        Database.getInstance().getAllBookings().clear();
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
    void testPlayerFullJourney() throws Exception {
        // Pre-condition: Approved Trainer
        Trainer t = new Trainer("Coach", "c@c.com", "pw", "Yoga", 20.0);
        t.setApproved(true);
        Database.getInstance().getAllUsers().add(t);
        String tId = t.getId().substring(0, 4);

        String input = 
            // 1. Register
            "2\nP1\np@p.com\npw\n1\n" +
            // 2. Login
            "1\np@p.com\npw\n" +
            // 3. Create Session
            "1\nYoga\nPark\n2025-12-01 10:00\n5\n" +
            // 4. Search & Book Trainer (2 hours = $40)
            "3\nYoga\n" + tId + "\n2\n" +
            // 5. Logout
            "5\n3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("Session created successfully"));
        assertTrue(output.contains("Booking successful! Paid $40.00"));
        
        // Verify DB
        assertEquals(1, Database.getInstance().getAllSessions().size());
        assertEquals(1, Database.getInstance().getAllBookings().size());
    }
}