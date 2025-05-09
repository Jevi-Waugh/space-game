package game.core;
import game.GameModel;

import game.core.Ship;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;



public class ShipTest {
    private Ship ship;

    @Before
    public void setup() {
        ship = new Ship();
    }

    @Test
    public void testXEqualsFive() {
        assertEquals(5, ship.getX());
    }

    @Test
    public void testYEqualsTen() {
        assertEquals(10, ship.getY());
    }

    @Test
    public void testHealthEqualsHundred() {
        assertEquals(100, ship.getHealth());
    }


}
