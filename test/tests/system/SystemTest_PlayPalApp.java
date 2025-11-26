package tests.system;

import app.main.PlayPalApp;
import app.managers.Database;
import app.utils.InputValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest_PlayPalApp {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        Database.getInstance().getAllUsers().clear(); 
        // No mock data needed for basic menu tests
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setIn(originalIn);
        System.setOut(originalOut);
        injectScanner(new Scanner(System.in));
    }

    private void prepareInput(String data) throws Exception {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
        injectScanner(new Scanner(testIn));
    }

    private void injectScanner(Scanner s) throws Exception {
        Field f = InputValidator.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, s);
    }

    @Test
    void testAppStartAndImmediateExit() throws Exception {
        // Input: 3 (Exit)
        prepareInput("3\n");
        PlayPalApp.main(new String[]{});
        
        String output = outContent.toString();
        assertTrue(output.contains("Welcome to PlayPal CLI"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void testInvalidMainMenuChoice() throws Exception {
        // Input: 99 (Invalid) -> 3 (Exit)
        prepareInput("99\n3\n");
        PlayPalApp.main(new String[]{});
        
        String output = outContent.toString();
        assertTrue(output.contains("Invalid choice"));
    }
    
    @Test
    void testInputErrorHandling() throws Exception {
        // Input: "abc" (Not int) -> 3 (Exit)
        prepareInput("abc\n3\n");
        PlayPalApp.main(new String[]{});
        
        String output = outContent.toString();
        assertTrue(output.contains("Invalid input. Please enter a whole number"));
    }
}