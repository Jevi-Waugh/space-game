package game;

import game.achievements.PlayerStatsTracker;
import game.core.Ship;
import game.utility.Logger;
import game.ui.UI;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import static org.junit.Assert.*;


public class GameModelTest {

    private GameModel model;
    private Ship ship;
    private TestUI testUI;


    private static class TestUI implements UI {
        List<String> logs = new ArrayList<>();

        public void start() {}
        public void pause() {}
        public void stop() {}
        public void onStep(game.ui.Tickable tickable) {}
        public void onKey(game.ui.KeyHandler key) {}
        public void render(java.util.List<game.core.SpaceObject> objects) {}
        public void log(String message) {
            logs.add(message);
        }
        public void setStat(String label, String value) {}
        public void logAchievementMastered(String message) {}
        public void logAchievements(java.util.List<game.achievements.Achievement> achievements) {}
        public void setAchievementProgressStat(String name, double progress) {}
    }


    @Before
    public void setUp() {

        testUI = new TestUI();
        Logger logger = testUI::log;
        model = new GameModel(logger, new PlayerStatsTracker(0L));


        // Create a default ship and make health 0
        ship = new Ship();
        ship.takeDamage(100);


        try {
            java.lang.reflect.Field f = GameModel.class.getDeclaredField("ship");
            f.setAccessible(true);
            f.set(model, ship);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    //------------CHECKING GAME OVER---------------------
    @Test
    public void testCheckGameOver_healthIsZero() {
        assertTrue(model.checkGameOver());
    }

    @Test
    public void testCheckGameOver_healthBelowZero() {
        ship.heal(-1);
        assertEquals(-1, ship.getHealth());
        assertTrue(model.checkGameOver());
    }

    @Test
    public void testCheckGameOver_healthAboveZero() {
        ship.heal(5);
        assertFalse(model.checkGameOver());
    }

    //----------------FIRE BULLET -----------------------
    @Test
    public void testFireBullet() {
        ship.heal(100);
        int startX = ship.getX();
        int startY = ship.getY();

        int initialSpaceObjectsLength = model.getSpaceObjects().size();

        model.fireBullet();

        assertEquals(initialSpaceObjectsLength + 1, model.getSpaceObjects().size());

        var lastObjectBullet = model.getSpaceObjects().getLast();

        assertTrue(lastObjectBullet instanceof game.core.Bullet);


        assertEquals(startX, lastObjectBullet.getX());
        assertEquals(startY, lastObjectBullet.getY());

    }

    @Test
    public void testFireBulletDoesNotLogAnything() {
        model.fireBullet();
        assertTrue("No Logging for fire bullet", testUI.logs.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testFireBulletWithN0Ship() throws Exception {
        Field f = GameModel.class.getDeclaredField("ship");
        f.setAccessible(true);
        f.set(model, null);
        model.fireBullet();
    }

    //-----------LEVEL UP------------------------------------
    private int getLevel() throws Exception {
        Field levelField = GameModel.class.getDeclaredField("lvl");
        levelField.setAccessible(true);
        return (int) levelField.get(model);
    }

    private int getSpawnRate() throws Exception {
        Field spawnRateField = GameModel.class.getDeclaredField("spawnRate");
        spawnRateField.setAccessible(true);
        return (int) spawnRateField.get(model);
    }

    private int getScoreThreshold() throws Exception {
        Field ScoreThreshField = GameModel.class.getDeclaredField("SCORE_THRESHOLD");
        ScoreThreshField.setAccessible(true);
        return ScoreThreshField.getInt(null);
    }

    private int getSpawnRateIncrease() throws Exception {
        Field rateIncreaseField = GameModel.class.getDeclaredField("SPAWN_RATE_INCREASE");
        rateIncreaseField.setAccessible(true);
        return rateIncreaseField.getInt(null);
    }

    @Test
    public void testLevelUpAtExactlyTheThreshold() throws Exception {
        // (ship.getScore() equals (lvl * SCORE_THRESHOLD))
        int lvlBefore = getLevel();
        int threshold = lvlBefore * getScoreThreshold();
        ship.addScore(threshold);

        model.levelUp();
        // model started at 1
        assertEquals("Level should be incremented by one more when score reaches the threshold exactly", lvlBefore+1, getLevel());
    }

    @Test
    public void testLevelUpAboveThreshold() throws Exception {
        int lvlBefore = getLevel();
        ship.addScore(lvlBefore * getScoreThreshold() * 100);
        model.levelUp();
        assertEquals(lvlBefore+1, getLevel());
    }

    @Test
    public void testNoLevelUpIfThresholdIsTooLow() throws Exception {
        // score = 0 
        ship.addScore(0); 
        int beforeLevel = getLevel();
        int beforeSpawnRate = getSpawnRate();

        model.levelUp();

        assertEquals(beforeLevel, getLevel());
        assertEquals(beforeSpawnRate, getSpawnRate());
    }



    @Test
    public void testLevelUpAndSpawnRateIncrement() throws Exception {
        ship.addScore(getLevel() * getScoreThreshold());
        int currentSpawnRate = getSpawnRate();
        model.levelUp();

        assertEquals(currentSpawnRate + getSpawnRateIncrease(), getSpawnRate());
    }

    @Test
    public void testLevelUpLoggingOnlyWhenVerboseIsTrue() throws Exception{
        model.setVerbose(true);
        ship.addScore(getLevel() * getScoreThreshold());

        testUI.logs.clear();
        model.levelUp();

        assertTrue(testUI.logs.stream().anyMatch(log -> log.contains("Level Up")));
    }

    @Test
    public void testLevelUpLoggingOnlyWhenVerboseIsFalse() throws Exception{
        model.setVerbose(false);
        ship.addScore(getLevel() * getScoreThreshold());

        testUI.logs.clear();
        model.levelUp();

        assertTrue(testUI.logs.isEmpty());
    }




























}
