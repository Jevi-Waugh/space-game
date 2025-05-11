package game;

import game.GameController;
import game.GameModel;
import game.achievements.AchievementManager;

import game.ui.UI;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameControllerTest {

    private GameController controller;
    private UI mockGui;
    private GameModel mockModel;

    @Before
    public void setUp() {
        mockGui = mock(UI.class);
        AchievementManager mockAchievements = mock(AchievementManager.class);
        mockModel = mock(GameModel.class);
        controller = new GameController(mockGui, mockModel, mockAchievements);

    }
    //-------------TEST PAUSE METHOD-------------------

    @Test
    public void testPausedReflectionUsed() throws Exception {
        // checks that pause is initially set to false
        Field paused = GameController.class.getDeclaredField("paused");
        // disables java checks
        paused.setAccessible(true);

        boolean value = (boolean) paused.get(controller);
        assertFalse(value);
    }

    @Test
    public void testPauseGame() {
        // paused is initially false
        // now it is true
        controller.pauseGame();
        verify(mockGui).pause();
        verify(mockGui).log("Game paused.");
    }


    @Test
    public void testUnpausedGame() {
        // paused = true
        controller.pauseGame();
        // now should unpause
        controller.pauseGame();

        verify(mockGui, times(2)).pause();
        verify(mockGui).log("Game paused.");
        // correct second message
        verify(mockGui).log("Game unpaused.");
    }

    //-------------TEST VERBOSE-------------------

    @Test
    public void checkVerboseVariable() throws Exception{
        Field verbose = GameController.class.getDeclaredField("isVerbose");
        verbose.setAccessible(true);

        boolean value = (boolean) verbose.get(controller);
        assertFalse(value);
    }

    @Test
    public void checkVerboseMethod() throws Exception{
        controller.setVerbose(true);
        Field verbose = GameController.class.getDeclaredField("isVerbose");
        verbose.setAccessible(true);

        boolean value = (boolean) verbose.get(controller);
        assertTrue(value);
        verify(mockModel).setVerbose(true);
    }

    //---------------------startTime-------------
    @Test
    public void testStartTimeExistsNotEndTime() throws Exception {
        Field field = GameController.class.getDeclaredField("startTime");
        assertEquals("startTime", field.getName());
        //asserting type
        assertEquals(long.class, field.getType());


    }
    //-------------------Achievement Manager--------------
    @Test
    public void testAchievementManagerExists() throws Exception {
        Field field = GameController.class.getDeclaredField("achievementManager");
        assertEquals("achievementManager", field.getName());
        assertEquals(AchievementManager.class, field.getType());

    }


}
