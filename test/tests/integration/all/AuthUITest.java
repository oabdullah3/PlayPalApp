package tests.integration.all;

import app.entities.User;
import app.managers.AuthManager;
import app.ui.AuthUI;
import app.utils.InputValidator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthUITest {

    private static AuthUI authUI;
    private static AuthManager authManager;
    private static String duplicateTargetEmail;
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void setupGlobal() {
        authUI = new AuthUI();
        authManager = AuthManager.getInstance();
        duplicateTargetEmail = "dup_" + UUID.randomUUID() + "@test.com";
    }

    @BeforeEach
    void setUp() {
        authManager.logout();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private void provideInput(String data) {
        try {
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
            Field field = InputValidator.class.getDeclaredField("scanner");
            field.setAccessible(true);
            field.set(null, mockScanner);
        } catch (Exception e) { fail(e.getMessage()); }
    }

    @Test
    @Order(1)
    void testHandleRegistration_Player_Success() {
        String inputs = "UI Player\n" + duplicateTargetEmail + "\npass123\n1\n";
        provideInput(inputs);

        authUI.handleRegistration();

        assertTrue(outContent.toString().contains("Registration successful"));
    }

    @Test
    @Order(2)
    void testHandleRegistration_DuplicateEmail() {
        String inputs = "Copy Cat\n" + duplicateTargetEmail + "\npass123\n1\n";
        provideInput(inputs);

        authUI.handleRegistration();
    }

    @Test
    @Order(3)
    void testHandleRegistration_Trainer_Success() {
        String uniqueEmail = "ui_trainer_" + UUID.randomUUID() + "@test.com";
        String inputs = "UI Coach\n" + uniqueEmail + "\npass123\n2\nPilates\n60.50\n";
        provideInput(inputs);

        authUI.handleRegistration();

        assertTrue(outContent.toString().contains("Trainer (Pending Approval)"));
    }

    @Test
    @Order(4)
    void testHandleLogin_Success() {
        String inputs = duplicateTargetEmail + "\npass123\n";
        provideInput(inputs);
        User loggedIn = authUI.handleLogin();
        assertNotNull(loggedIn);
    }

    @Test
    @Order(5)
    void testHandleLogin_Failure() {
        String inputs = "ghost@ghost.com\nwrongpass\n";
        provideInput(inputs);
        User result = authUI.handleLogin();
        assertNull(result);
    }
}