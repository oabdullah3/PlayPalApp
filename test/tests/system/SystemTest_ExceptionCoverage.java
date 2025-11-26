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

class SystemTest_ExceptionCoverage {
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    // Also capture Out to debug if needed, though strict check is on err
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream(); 

    @BeforeEach
    void setUp() {
        System.setErr(new PrintStream(errContent)); 
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
        Database.getInstance().getAllSessions().clear();
        Database.getInstance().getAllBookings().clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setErr(System.err);
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
    void testExceptions() throws Exception {
        // 1. Setup Players
        Player p1 = new Player("P1", "p1@test.com", "pw");
        Player p2 = new Player("P2", "p2@test.com", "pw");
        Database.getInstance().getAllUsers().add(p1);
        Database.getInstance().getAllUsers().add(p2);

        // --- Setup for Case A/B (Sessions) ---
        // Create session (Creator + 1 spot = Not Full initially)
        Session raceSession = new Session(p1.getId(), "RaceSport", "Loc", LocalDateTime.now(), 2);
        Database.getInstance().getAllSessions().add(raceSession);
        // Note: We skip simulating the 'Full' race condition as it's hard in CLI flow
        
        // --- FIX: Setup for Case C (Trainer Search) ---
        // We MUST have a trainer for "Yoga" so the search succeeds and asks for ID
        Trainer dummyTrainer = new Trainer("Dummy", "t@t.com", "pw", "Yoga", 50.0);
        dummyTrainer.setApproved(true);
        Database.getInstance().getAllUsers().add(dummyTrainer);

        // IDs
        String p2Id = p2.getId().substring(0, 4);

        String input = 
            "1\np2@test.com\npw\n" +    // Login Player 2
            
            // --- Case A: SessionNotFoundException ---
            "2\nRaceSport\n" +          // Search
            "INVALID_ID\n" +            // Enter wrong ID
            
            // --- Case C: BookingFailedException (Not a Trainer) ---
            "3\nYoga\n" +               // Search Trainer (Finds Dummy)
            p2Id + "\n" +               // Enter Player ID (This triggers the error)
            "1\n" +                     // Hours (Input consumed before error check logic finishes)
            
            "5\n3\n";                   // Logout, Exit

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String errors = errContent.toString();
        
        // Assertions
        assertTrue(errors.contains("SessionNotFoundException"));
        assertTrue(errors.contains("BookingFailedException"));
        assertTrue(errors.contains("not a Trainer") || errors.contains("Invalid trainer ID"));
    }
}