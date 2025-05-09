package game.core;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

public class ShieldPowerUpTest {

    private ShieldPowerUp shieldPowerUp;
    private Ship ship;

    @Before
    public void setUp() {
        shieldPowerUp = new ShieldPowerUp(5, 10);
        ship = new Ship();

    }

    @Test
    public void testApplyEffectAddsScoreBy50() {
        int initialScore = ship.getScore();
        shieldPowerUp.applyEffect(ship);
        assertEquals(initialScore + 50, ship.getScore());
    }

    @Test
    public void testApplyEffectTwiceAddsScoreBy50Twice() {
        int initialScore = ship.getScore();
        shieldPowerUp.applyEffect(ship);
        shieldPowerUp.applyEffect(ship);
        assertEquals(initialScore + 100, ship.getScore());
    }

    @Test(expected = NullPointerException.class)
    public void testApplyEffectWithNullShipThrowException() {
        shieldPowerUp.applyEffect(null);
    }




}

