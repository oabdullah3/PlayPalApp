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
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class SystemTest_MessagingTwoWay {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
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
    void testTrainerPlayerConversation() throws Exception {
        // 1. Setup Users
        Player player = new Player("Player1", "p@p.com", "pw");
        Trainer trainer = new Trainer("Coach", "t@t.com", "pw", "Yoga", 50.0);
        trainer.setApproved(true);
        
        Database.getInstance().getAllUsers().add(player);
        Database.getInstance().getAllUsers().add(trainer);
        
        String pId = player.getId().substring(0, 4);
        String tId = trainer.getId().substring(0, 4);

        String input = 
            // --- STEP 1: Player sends message to Trainer ---
            "1\np@p.com\npw\n" +        // Login Player
            "4\n" +                     // View Messages
            tId + "\n" +                // Send to Trainer
            "Hello Coach!\n" +          // Content
            "5\n" +                     // Logout

            // --- STEP 2: Trainer sees message and replies ---
            "1\nt@t.com\npw\n" +        // Login Trainer
            "3\n" +                     // View Messages (Should see "Hello Coach!")
            pId + "\n" +                // Reply to Player
            "Hi Player, ready?\n" +     // Content
            "4\n" +                     // Logout

            // --- STEP 3: Player sees reply ---
            "1\np@p.com\npw\n" +        // Login Player
            "4\n" +                     // View Messages (Should see "Hi Player...")
            "0\n" +                     // Return (Don't send new)
            "5\n3\n";                   // Logout, Exit

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        
        // Assertions for Data Flow
        assertTrue(output.contains("Message sent successfully"));
        assertTrue(output.contains("From " + pId + ": Hello Coach!")); // Trainer seeing player msg
        assertTrue(output.contains("From " + tId + ": Hi Player, ready?")); // Player seeing trainer msg
        
        // Verify Message Entities covered in DB
        assertEquals(2, Database.getInstance().getAllMessages().size());
    }
}