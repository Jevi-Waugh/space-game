package game;

import game.achievements.AchievementManager;
import game.achievements.PlayerStatsTracker;
import game.core.Ship;
import game.ui.UI;
import game.utility.Direction;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GameControllerTest {

    private GameController controller;
    private TestUI testUI;
    private TestGameModel testModel;

    // ------------MANUALLY MOCKING CLASSES-----------------
    private static class TestUI implements UI {
        boolean isPaused = false;
        int counter = 0;
        String lastLog = "";
        List<String> logs = new ArrayList<>();

        @Override
        public void pause() {
            isPaused = true;
            counter++;
        }

        @Override
        public void log(String message) {
            lastLog = message;
            logs.add(message);
        }

        // Unused methods with empty bodies
        public void start() {}
        public void stop() {}
        public void onStep(game.ui.Tickable t) {}
        public void onKey(game.ui.KeyHandler k) {}
        public void render(java.util.List list) {}
        public void setStat(String a, String b) {}
        public void logAchievementMastered(String msg) {}
        public void logAchievements(java.util.List<game.achievements.Achievement> achievements) {}
        public void setAchievementProgressStat(String achievementName, double progressPercentage) {}
    }

    private static class TestGameModel extends GameModel {
        boolean verboseSet = false;

        public TestGameModel() {
            super((msg) -> {}, new PlayerStatsTracker());
        }

        @Override
        public void setVerbose(boolean verbose) {
            this.verboseSet = verbose;
        }
    }

    private static class TestAchievementManager extends AchievementManager {
        public TestAchievementManager() {
            super(new TestAchievementFile());
        }
    }

    private static class TestAchievementFile implements game.achievements.AchievementFile {
        @Override
        public void setFileLocation(String fileLocation) {

        }

        @Override
        public String getFileLocation() {
            return "";
        }

        @Override
        public void save(String achievementName) {
            // no-op
        }

        @Override
        public List<String> read() {
            return List.of();
        }


    }

    private static class TestGameModelAndShip extends GameModel {
        static String lastCall = null;
        static boolean fireBulletCalled = false;
        static boolean recordShotFiredCalled = false;

        TestShip ship = new TestShip();

        public TestGameModelAndShip() {
            super(message -> {}, new TestPlayerStatsTracker());
        }

        @Override
        public Ship getShip() {
            return ship;
        }

        @Override
        public void fireBullet() {
            fireBulletCalled = true;
        }

        @Override
        public PlayerStatsTracker getStatsTracker() {
            return (PlayerStatsTracker) super.getStatsTracker(); // returns our test subclass
        }

        @Override
        public void checkCollisions() {
            lastCall += " and some collision";
        }

        static class TestShip extends Ship {
            public TestShip() {
                super(10, 10, 100);
            }

            @Override
            public void move(Direction direction) {
                lastCall = direction.name();
            }
        }

        static class TestPlayerStatsTracker extends PlayerStatsTracker {
            @Override
            public void recordShotFired() {
                recordShotFiredCalled = true;
            }
        }
    }


    //-------------SETUP------------------
    @Before
    public void setUp() {
        testUI = new TestUI();
        testModel = new TestGameModel();
        TestAchievementManager testAchievements = new TestAchievementManager();
        controller = new GameController(testUI, testModel, testAchievements);
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
        assertTrue(testUI.isPaused);
        assertEquals("Game paused.", testUI.lastLog);
    }


    @Test
    public void testUnpausedGame() {
        // paused = true
        controller.pauseGame();
        // now should unpause
        controller.pauseGame();

        assertEquals(2, testUI.counter);
        assertTrue(testUI.logs.contains("Game paused."));
        assertTrue(testUI.logs.contains("Game unpaused."));
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
        assertTrue(testModel.verboseSet);
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
    //-----------------HANDLE PLAYER INPUT---------------------
    @Test
    public void testHandlePlayerInputUp() {
        String[] up = {"W", "w"};
        for (String input: up) {
            TestGameModelAndShip.lastCall = null;
            controller = new GameController(new TestUI(), new TestGameModelAndShip(), new TestAchievementManager());
            controller.handlePlayerInput(input);
            assertEquals("Fails due to" + input, "UP and some collision", TestGameModelAndShip.lastCall);
        }

    }

    @Test
    public void testHandlePlayerInputDown() {
        String[] down = {"S", "s"};
        for (String input: down) {
            TestGameModelAndShip.lastCall = null;
            controller = new GameController(new TestUI(), new TestGameModelAndShip(), new TestAchievementManager());
            controller.handlePlayerInput(input);
            assertEquals("Fails due to" + input, "DOWN and some collision", TestGameModelAndShip.lastCall);
        }

    }

    @Test
    public void testHandlePlayerInputLeft() {
        String[] left = {"A", "a"};
        for (String input: left) {
            TestGameModelAndShip.lastCall = null;
            controller = new GameController(new TestUI(), new TestGameModelAndShip(), new TestAchievementManager());
            controller.handlePlayerInput(input);
            assertEquals("Fails due to" + input, "LEFT and some collision", TestGameModelAndShip.lastCall);
        }

    }

    @Test
    public void testHandlePlayerInputRight() {
        String[] right = {"D", "d"};
        for (String input: right) {
            TestGameModelAndShip.lastCall = null;
            controller = new GameController(new TestUI(), new TestGameModelAndShip(), new TestAchievementManager());
            controller.handlePlayerInput(input);
            assertEquals("Fails due to" + input, "RIGHT and some collision", TestGameModelAndShip.lastCall);
        }

    }

    @Test
    public void testHandlePlayerInputFireBulletAndRecordShot() {
        controller = new GameController(testUI, new TestGameModelAndShip(), new TestAchievementManager());
        controller.handlePlayerInput("F");

        assertTrue(TestGameModelAndShip.fireBulletCalled);
        assertTrue(TestGameModelAndShip.recordShotFiredCalled);
    }

    @Test
    public void testHandlePlayerInputInvalidInput() {
        String[] invalidInput = {"Z", "z"};
        for (String input: invalidInput) {
            TestGameModelAndShip.lastCall = null;
            TestUI ui = new TestUI();
            controller = new GameController(ui, new TestGameModelAndShip(), new TestAchievementManager());
            controller.handlePlayerInput(input);
            assertEquals("Invalid input. Use W, A, S, D, F, or P.", ui.lastLog);
        }

    }

    @Test
    public void testHandlePlayerInputWhilePausedIsIgnored() throws Exception {
        TestGameModelAndShip.lastCall = null;
        controller = new GameController(testUI, new TestGameModelAndShip(), new TestAchievementManager());

        // Pause the game
        controller.pauseGame();

        // Try moving while paused
        controller.handlePlayerInput("A");
        controller.handlePlayerInput("a");

        // Movement should be ignored
        assertNull(TestGameModelAndShip.lastCall);
    }








}
