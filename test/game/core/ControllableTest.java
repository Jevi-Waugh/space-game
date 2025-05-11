package game.core;

import game.GameModel;
import game.exceptions.BoundaryExceededException;
import game.ui.ObjectGraphic;
import game.utility.Direction;
import org.junit.Test;
import static game.GameModel.GAME_WIDTH;
import static game.GameModel.GAME_HEIGHT;


import org.junit.Before;
import static org.junit.Assert.*;

public class ControllableTest {


    public static class TestControllable extends Controllable {

        public TestControllable(int x, int y) {
            super(x, y);
        }

        @Override
        public ObjectGraphic render() {
            return null;
        }

        @Override
        public void tick(int tick) {}
    }

    private TestControllable controllable;

    @Before
    public void setUp() {
        controllable = new TestControllable(1, 1);
    }

    @Test
    public void testMoveUpWithinBounds() throws BoundaryExceededException {
        controllable.move(Direction.UP);
        assertEquals(0, controllable.getY());
    }

    @Test
    public void testMoveDownWithinBounds() throws BoundaryExceededException {
        controllable.move(Direction.DOWN);
        assertEquals(2, controllable.getY());
    }

    @Test
    public void testMoveLeftWithinBounds() throws BoundaryExceededException {
        controllable.move(Direction.LEFT);
        assertEquals(0, controllable.getX());
    }

    @Test
    public void testMoveRightWithinBounds() throws BoundaryExceededException {
        controllable.move(Direction.RIGHT);
        assertEquals(2, controllable.getX());
    }

    @Test(expected = BoundaryExceededException.class)
    public void testMoveUpOutOfBounds() throws BoundaryExceededException {
        controllable = new TestControllable(2, 0);
        controllable.move(Direction.UP);
    }

    @Test(expected = BoundaryExceededException.class)
    public void testMoveDownOutOfBounds() throws BoundaryExceededException {
        controllable = new TestControllable(5, GAME_HEIGHT - 1);
        controllable.move(Direction.DOWN);
    }

    @Test(expected = BoundaryExceededException.class)
    public void testMoveLeftOutOfBounds() throws BoundaryExceededException {
        controllable = new TestControllable(0, 5);
        controllable.move(Direction.LEFT);
    }

    @Test(expected = BoundaryExceededException.class)
    public void testMoveRightOutOfBounds() throws BoundaryExceededException {
        controllable = new TestControllable(GAME_WIDTH - 1, 5);
        controllable.move(Direction.RIGHT);
    }

    @Test
    public void testMoveThrowsCorrectException() {
        TestControllable c = new TestControllable(GameModel.GAME_WIDTH - 1, 5);

        try {
            c.move(Direction.RIGHT);
            fail("Expected BoundaryExceededException to be thrown");
        } catch (BoundaryExceededException bee) {
            assertEquals("Cannot move " + Direction.RIGHT.name().toLowerCase() + ". Out of bounds!", bee.getMessage());
        } catch (Exception e) {
            fail("Expected BoundaryExceededException, but instead got: " + e.getClass().getSimpleName());
        }
    }

}

