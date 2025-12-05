package tests.unit.all;

import app.entities.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testPlayerDefaults() {
        // Covers: Player constructor and default balance constant
        Player p = new Player("P1", "p@p.com", "pass");
        
        assertEquals(50.0, p.getBalance(), "Player should default to 50.0 balance");
    }

    @Test
    void testPlayerMenuOptions() {
        // Covers: Player.showMenuOptions (executes the print statements)
        Player p = new Player("P1", "p@p.com", "pass");
        
        // This is a void method that prints to console. 
        // We run it to ensure no runtime errors occur during execution.
        assertDoesNotThrow(() -> p.showMenuOptions());
    }
}