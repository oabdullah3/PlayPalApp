package tests.unit.all;

import app.entities.Player;
import app.entities.Trainer;
import app.entities.User;
import app.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // --- Factory Method Coverage (Branching) ---

    @Test
    void testCreateFactoryReturnsPlayer() {
        // Covers: User.create -> else branch
        User u = User.create("PlayerOne", "p1@test.com", "pass", false, null, 0.0);
        
        assertTrue(u instanceof Player);
        assertEquals("PlayerOne", u.getName());
    }

    @Test
    void testCreateFactoryReturnsTrainer() {
        // Covers: User.create -> if (isTrainer) branch
        User u = User.create("TrainerOne", "t1@test.com", "pass", true, "Yoga", 50.0);
        
        assertTrue(u instanceof Trainer);
        assertEquals("Yoga", ((Trainer) u).getSpecialty());
    }

    // --- Payment Logic Coverage (Branching) ---

    @Test
    void testPaySuccess() throws InsufficientFundsException {
        // Covers: User.pay -> if (balance < amount) is FALSE
        Player p = new Player("Richie", "rich@test.com", "pass");
        p.setBalance(100.0); 
        
        p.pay(40.0);
        
        assertEquals(60.0, p.getBalance(), 0.001);
    }

    @Test
    void testPayInsufficientFunds() {
        // Covers: User.pay -> if (balance < amount) is TRUE
        Player p = new Player("Broke", "broke@test.com", "pass");
        p.setBalance(10.0);

        Exception exception = assertThrows(InsufficientFundsException.class, () -> {
            p.pay(50.0);
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    void testReceivePayment() {
        // Covers: User.receivePayment
        Player p = new Player("Earner", "earn@test.com", "pass");
        p.setBalance(0.0);
        
        p.receivePayment(100.0);
        
        assertEquals(100.0, p.getBalance(), 0.001);
    }

    // --- Common Getters & Setters ---

    @Test
    void testUserGettersAndSetters() {
        // Covers: setId, getId, setName, getEmail, getPassword, setBalance
        User u = new Player("OldName", "email@test.com", "secret");
        
        u.setId("custom-id-123");
        u.setName("NewName");
        u.setBalance(500.0);

        assertEquals("custom-id-123", u.getId());
        assertEquals("NewName", u.getName());
        assertEquals("email@test.com", u.getEmail());
        assertEquals("secret", u.getPassword());
        assertEquals(500.0, u.getBalance(), 0.001);
    }
}