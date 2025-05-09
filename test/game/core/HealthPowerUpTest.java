package game.core;

import org.junit.Test;


import org.junit.Before;

import static org.junit.Assert.*;

public class HealthPowerUpTest {

    private HealthPowerUp powerUp;
    private Ship ship;

    @Before
    public void setUp() {
        powerUp = new HealthPowerUp(1, 1);
        ship = new Ship(0, 0,10);
    }

    @Test
    public void testApplyEffect_limitation1() {
        // ship.heal(5);
        powerUp.applyEffect(ship);
        assertEquals(30, ship.getHealth());
    }
}
