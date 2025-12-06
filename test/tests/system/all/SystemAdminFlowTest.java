package tests.system.all;

import app.main.PlayPalApp;
import app.entities.Player;
import app.entities.Trainer;
import app.managers.AuthManager;
import app.managers.Database;
import app.managers.SystemManager;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemAdminFlowTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        
        AuthManager.getInstance().logout();
        
        try {
            Database db = Database.getInstance();
            for (Field field : Database.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    ((Map<?, ?>) field.get(db)).clear();
                }
            }
        } catch (Exception e) {}
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        try {
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, new Scanner(System.in));
        } catch (Exception e) {
            fail("Failed to reset scanner: " + e.getMessage());
        }
    }

    @Test
    void testAdminDashboardFlow() {
        String adminEmail = "admin@playpal.com";
        String adminPass = "admin123";
        Player admin = new Player("admin", adminEmail, adminPass);
        Database.getInstance().addUser(admin);

        String trainerEmail = "pending_t@" + UUID.randomUUID() + ".com";
        Trainer pendingTrainer = new Trainer("Pending Coach", trainerEmail, "pass", "Yoga", 50.0);
        pendingTrainer.setApproved(false);
        Database.getInstance().addUser(pendingTrainer);

        String trainerIdPrefix = pendingTrainer.getId().substring(0, 4);

        StringBuilder input = new StringBuilder();
        
        input.append("1\n");               
        input.append(adminEmail + "\n");    
        input.append(adminPass + "\n");     

        input.append("1\n");                
        
        input.append(trainerIdPrefix + "\n"); 

        input.append("2\n");                

        input.append("3\n");

        setInput(input.toString());
        
        try {
            PlayPalApp.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fullLog = outContent.toString() + "\nERRORS:\n" + errContent.toString();

        assertTrue(fullLog.contains("ADMIN DASHBOARD"), 
            "Should enter Admin Dashboard.\nLog:\n" + fullLog);
            
        assertTrue(fullLog.contains("System Status Report") || fullLog.contains("Total Users"), 
            "Should automatically display system stats.\nLog:\n" + fullLog);

        assertTrue(fullLog.contains("approved successfully"), 
            "Should verify trainer approval message.\nLog:\n" + fullLog);
    }
}