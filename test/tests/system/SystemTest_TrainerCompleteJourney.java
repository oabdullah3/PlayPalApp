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

class SystemTest_TrainerCompleteJourney {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear();
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
    void testTrainerJourney() throws Exception {
        // Pre-condition: Existing Booking to View
        Trainer t = new Trainer("OldName", "t@t.com", "pw", "OldSpec", 10.0);
        t.setApproved(true);
        Database.getInstance().getAllUsers().add(t);
        Booking b = new Booking("player-id", t.getId(), 50.0);
        Database.getInstance().getAllBookings().add(b);

        String input = 
        	    "1\nt@t.com\npw\n" + 
        	    "1\n" +
        	    "2\nNewName\n99.0\nNewSpec\n" + // Provide all 3 values explicitly
        	    "4\n3\n";

        prepareInput(input);
        PlayPalApp.main(new String[]{});

        String output = outContent.toString();
        // Verify Booking View
        assertTrue(output.contains("Trainer Bookings"));
        assertTrue(output.contains("Booking ID"));
        
        // Verify Profile Update logic in DB
        assertEquals("NewName", t.getName());
        assertEquals("NewSpec", t.getSpecialty());
        assertEquals(99.0, t.getHourlyRate()); // Unchanged
    }
}