package game.core;

import org.junit.Test;


import org.junit.Before;

import static org.junit.Assert.*;

public class HealthPowerUpTest {

    private HealthPowerUp powerUp;
    private Ship ship;
    int maxHealth = 100;

    @Before
    public void setUp() {
        powerUp = new HealthPowerUp(0, 0);
        ship = new Ship(0, 0,0);
    }

    @Test
    public void testApplyHealthFromZero() {
        powerUp.applyEffect(ship);
        assertEquals(20, ship.getHealth());
    }

    @Test(expected = NullPointerException.class)
    public void testApplyHealthWithNullShipThrowException() {
        powerUp.applyEffect(null);
    }


    @Test
    public void testApplyHealthWithNegativeHealth() {
        ship = new Ship(0, 0,-50);
        powerUp.applyEffect(ship);
        assertEquals(-30, ship.getHealth());
    }

    @Test
    public void testApplyHealthMaxHealth() {
        int counter = 0;
        for (int i = 0; i < 5; i++) {
            powerUp.applyEffect(ship);
            counter = counter + 20;
        }
        assertEquals(counter, ship.getHealth());
    }

    @Test
    public void testApplyHealthNotExceedMaxHealth() {
        int counter = 0;
        for (int i = 0; i < 8; i++) {
            powerUp.applyEffect(ship);
            counter = counter + 20;
        }
        assertEquals(maxHealth, ship.getHealth());
    }

    @Test
    public void testApplyHealthCapping() {
        ship = new Ship(0, 0,90);
        powerUp.applyEffect(ship);
        assertEquals(maxHealth, ship.getHealth());
    }

}
