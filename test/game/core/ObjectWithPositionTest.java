package game.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectWithPositionTest {

    @Test
    public void testToStringForShip() {
        Ship ship = new Ship(0,0,0);
        String expectedString = "Ship(0, 0)";
        assertEquals(expectedString, ship.toString());
    }

    @Test
    public void testToStringForBullet() {
        Bullet bullet = new Bullet(1,1);
        String expectedString = "Bullet(1, 1)";
        assertEquals(expectedString, bullet.toString());
    }

    @Test
    public void testToStringForEnemy() {
        Enemy enemy = new Enemy(2,2);
        String expectedString = "Enemy(2, 2)";
        assertEquals(expectedString, enemy.toString());
    }

    @Test
    public void testToStringForAsteroid() {
        Asteroid asteroid = new Asteroid(3,3);
        String expectedString = "Asteroid(3, 3)";
        assertEquals(expectedString, asteroid.toString());
    }

    @Test
    public void testToStringForHealthPowerUp() {
        HealthPowerUp healthPowerUp = new HealthPowerUp(4,4);
        String expectedString = "HealthPowerUp(4, 4)";
        assertEquals(expectedString, healthPowerUp.toString());
    }

    @Test
    public void testToStringForShieldPowerUp() {
        ShieldPowerUp shieldPowerUp = new ShieldPowerUp(5,5);
        String expectedString = "ShieldPowerUp(5, 5)";
        assertEquals(expectedString, shieldPowerUp.toString());
    }



}