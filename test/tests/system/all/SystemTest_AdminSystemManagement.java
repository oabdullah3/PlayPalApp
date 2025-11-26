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

class SystemTest_AdminSystemManagement {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
        // Ensure Admin Exists
        Database.getInstance().getAllUsers().add(new Player("Admin", "admin@playpal.com", "admin123"));
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
    void testAdminApprovesTrainer() throws Exception {
        // 1. Add Pending Trainer
        Trainer t = new Trainer("PendingGuy", "p@p.com", "pw", "Run", 10.0);
        t.setApproved(false);
        Database.getInstance().getAllUsers().add(t);
        String tId = t.getId().substring(0, 4);

        String input = 
            // Login Admin
            "1\nadmin@playpal.com\nadmin123\n" +
            // 1. Approve -> Enter ID
            "1\n" + tId + "\n" +
            // 2. Verify Status Display (Implied on menu load)
            // 3. Logout
            "2\n3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("ADMIN DASHBOARD"));
        assertTrue(output.contains("has been approved!"));
        assertTrue(t.isApproved());
    }
    
    @Test
    void testAdminNoPendingRequests() throws Exception {
        String input = "1\nadmin@playpal.com\nadmin123\n1\n2\n3\n"; // Login -> Approve -> Logout
        prepareInput(input);
        PlayPalApp.main(new String[]{});
        
        assertTrue(outContent.toString().contains("No pending trainer requests"));
    }
}