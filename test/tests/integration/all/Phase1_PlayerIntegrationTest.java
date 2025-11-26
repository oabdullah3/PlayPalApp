package tests.integration.all;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import app.entities.Player;
import app.managers.Database;

/**
 * INTEGRATION PHASE 1: Player Constructor & Methods
 * 
 * Tests the Player class which depends on:
 * - User constructor (unit tested)
 * - PlayerState (unit tested)
 */
public class Phase1_PlayerIntegrationTest {

    @BeforeEach
    void setUp() {
        Database.getInstance().getAllUsers().clear();
    }

    @Test
    void testPlayerConstructor() {
        Player player = new Player("Alice Smith", "alice@example.com", "password123");

        assertNotNull(player.getId(), "Player ID should be auto-generated");
        assertEquals("Alice Smith", player.getName());
        assertEquals("alice@example.com", player.getEmail());
        assertEquals("password123", player.getPassword());
        assertEquals(50.0, player.getBalance(), "Default player balance should be 50.00");
    }

    @Test
    void testPlayerShowMenuOptions() {
        Player player = new Player("Bob Jones", "bob@example.com", "pass456");
        assertDoesNotThrow(() -> player.showMenuOptions());
    }

    @Test
    void testPlayerInheritedSetters() {
        Player player = new Player("Charlie Brown", "charlie@example.com", "pass789");

        player.setName("Charlie Updated");
        assertEquals("Charlie Updated", player.getName());

        player.setBalance(100.0);
        assertEquals(100.0, player.getBalance());
    }

    @Test
    void testPlayerExecuteMenu() {
        Player player = new Player("Diana Prince", "diana@example.com", "pass000");
        assertDoesNotThrow(() -> player.executeMenu());
    }

    @Test
    void testPlayerDefaultBalance() {
        Player player1 = new Player("Player 1", "p1@test.com", "pass");
        Player player2 = new Player("Player 2", "p2@test.com", "pass");
        
        assertEquals(50.0, player1.getBalance());
        assertEquals(50.0, player2.getBalance());
    }
}
