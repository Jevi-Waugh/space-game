package game.core;

import game.ui.ObjectGraphic;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PowerUpTest {

    private PowerUp powerUp;

    // Concrete test subclass
    private static class TestPowerUp extends PowerUp {
        public TestPowerUp(int x, int y) {
            super(x, y);
        }

        @Override
        public void applyEffect(Ship ship) {
            // No operation for testing
        }

        @Override
        public ObjectGraphic render() {
            return null;
        }
    }

    @Before
    public void setUp() {
        powerUp = new TestPowerUp(10, 10);
    }

    @Test
    public void testTickMultipleOf10() {
        powerUp.tick(10);
        assertEquals(11, powerUp.getY());
    }

    @Test
    public void testTickNotMultipleOf10() {
        powerUp.tick(7);
        assertEquals(10, powerUp.getY());
    }

    @Test
    public void testTickZeroIncrementsY() {
        powerUp.tick(0);
        assertEquals(11, powerUp.getY());
    }

    @Test
    public void testTickNegativeNumberThatIncrementsY() {
        powerUp.tick(-10);
        assertEquals(11, powerUp.getY());
    }

    @Test
    public void testTickNegativeNumberDoesNotIncrementY() {
        powerUp.tick(-6);
        assertEquals(10, powerUp.getY());
    }

}
