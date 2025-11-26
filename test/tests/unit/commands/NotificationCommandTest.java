package tests.unit.commands;

import app.patterns.command.NotificationCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class NotificationCommandTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() { System.setOut(new PrintStream(outContent)); }

    @AfterEach
    void tearDown() { System.setOut(originalOut); }

    @Test
    void testNotificationCommand() {
        // "user-1" is long enough to pass substring check
        NotificationCommand cmd = new NotificationCommand("user-1", "Alert");
        cmd.execute();
        assertTrue(outContent.toString().contains("ALERT to user"));
        assertTrue(outContent.toString().contains("Alert"));
    }
}