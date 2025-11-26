package app.unit.tests.utils;

import app.entities.Player;
import app.entities.Trainer;
import app.utils.PlayerState;
import app.utils.TrainerState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class StateTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() { System.setOut(new PrintStream(outContent)); }

    @AfterEach
    void tearDown() { System.setOut(originalOut); }

    // --- STUBS ---
    // We stub Player/Trainer because their real constructors depend on State (Circular)
    // We only need their getters for this test.
    static class StubPlayer extends Player {
        public StubPlayer() { super("Stub", "e", "p"); }
        @Override public String getName() { return "P_Name"; }
        @Override public double getBalance() { return 50.0; }
    }

    static class StubTrainer extends Trainer {
        private boolean approved;
        public StubTrainer(boolean app) { super("Stub", "e", "p", "s", 10.0); this.approved = app; }
        @Override public String getName() { return "T_Name"; }
        @Override public double getBalance() { return 100.0; }
        @Override public double getHourlyRate() { return 20.0; }
        @Override public boolean isApproved() { return approved; }
    }

    @Test
    void testPlayerState() {
        new PlayerState(new StubPlayer()).showMenu();
        String out = outContent.toString();
        assertTrue(out.contains("P_Name"));
        assertTrue(out.contains("$50.0"));
        assertTrue(out.contains("1. Create Session"));
    }

    @Test
    void testTrainerStateApproved() {
        new TrainerState(new StubTrainer(true)).showMenu();
        String out = outContent.toString();
        assertTrue(out.contains("T_Name"));
        assertTrue(out.contains("APPROVED"));
        assertTrue(out.contains("$20.0/hr"));
    }

    @Test
    void testTrainerStatePending() {
        new TrainerState(new StubTrainer(false)).showMenu();
        assertTrue(outContent.toString().contains("PENDING"));
    }
}