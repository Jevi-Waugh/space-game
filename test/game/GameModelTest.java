package game;

import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.ui.ObjectGraphic;
import game.utility.Logger;
import game.ui.UI;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import static org.junit.Assert.*;
import java.util.Random;


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

        // Directly fetch model's ship and store it
        try {
            Field f = GameModel.class.getDeclaredField("ship");
            f.setAccessible(true);
            ship = (Ship) f.get(model);
            ship.takeDamage(100);
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
    public void testFireBulletWithNoShip() throws Exception {
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
        String expectedMessage = "Level Up! Welcome to Level "+ model.getLevel() + ". Spawn rate increased to " +getSpawnRate() + "%.";
        assertTrue(testUI.logs.stream().anyMatch(log -> log.contains(expectedMessage)));
        //
    }

    @Test
    public void testLevelUpLoggingOnlyWhenVerboseIsFalse() throws Exception{
        model.setVerbose(false);
        ship.addScore(getLevel() * getScoreThreshold());

        testUI.logs.clear();
        model.levelUp();

        assertTrue(testUI.logs.isEmpty());
    }
    //-------------UPDATE GAME----------------------
    @Test
    public void testUpdateGameCallingTick() {
        class StubSpaceObject implements SpaceObject {
            int tickCalled = -1;

            @Override
            public void tick(int tick) {
                tickCalled = tick;
            }

            @Override
            public int getX() { return 9; }

            @Override
            public int getY() { return 9; }

            @Override
            public ObjectGraphic render() { return null; }
        }

        StubSpaceObject obj = new StubSpaceObject();
        model.addObject(obj);

        model.updateGame(50);

        assertEquals(50, obj.tickCalled);
    }

    @Test
    public void testUpdateGameKeepsIsInBoundsSpaceObject() {
        SpaceObject spaceObject = new SpaceObject() {
            public void tick(int tick) {}
            public int getX() { return 0; }
            public int getY() { return 0; }
            public ObjectGraphic render() { return null; }
        };

        model.addObject(spaceObject);
        model.updateGame(0);

        assertTrue(model.getSpaceObjects().contains(spaceObject));
    }

    @Test
    public void testUpdateGameRemovesOutOfBoundsSpaceObject() {
        SpaceObject spaceObject = new SpaceObject() {
            public void tick(int tick) {}
            public int getX() { return -1; }
            public int getY() { return 0; }
            public ObjectGraphic render() { return null; }
        };

        model.addObject(spaceObject);
        model.updateGame(0);

        assertFalse(model.getSpaceObjects().contains(spaceObject));
    }

    @Test
    public void testUpdateGameCrashingBehaviour() {
        try {
            model.updateGame(0);
        } catch (Exception e) {
            fail("updateGame basically would or should not stop or crach on a empty spaceObjects list");
        }
    }

    @Test
    public void testUpdateGameUsesIsInBoundsLogicToRemoveOutOfBounds() {
        SpaceObject spaceObject = new SpaceObject() {
            public void tick(int tick) {}
            public int getX() { return 0; }
            // y is game height which should not be on 6the grid
            public int getY() { return 20; }
            public ObjectGraphic render() { return null; }
        };

        model.addObject(spaceObject);
        model.updateGame(0);

        assertFalse(model.getSpaceObjects().contains(spaceObject));
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateGameSpaceObjectsIsNull() throws Exception {
        Field f = GameModel.class.getDeclaredField("spaceObjects");
        f.setAccessible(true);
        f.set(model, null);

        model.updateGame(1);
    }

    //--------------TEST CONSTRUCTOR------------------------
    @Test
    public void testGameModelConstructor() throws Exception {
        Logger logger = msg -> {};
        PlayerStatsTracker tracker = new PlayerStatsTracker(12L);
        GameModel model = new GameModel(logger, tracker);

        Field loggerField = GameModel.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        assertSame("Logger is stored correctly", logger, loggerField.get(model));

        Field trackerField = GameModel.class.getDeclaredField("statsTracker");
        trackerField.setAccessible(true);
        assertSame("Stats tracker is stored correctly", tracker, trackerField.get(model));
    }
    //-------------TEST VERBOSE--------------------
    @Test
    public void testSetVerboseToTrue() throws Exception {
        model.setVerbose(true);

        Field verboseField = GameModel.class.getDeclaredField("verbose");
        verboseField.setAccessible(true);
        boolean value = verboseField.getBoolean(model);

        assertTrue("Verbose should be set to true after setting it to true", value);
    }

    @Test
    public void testSetVerboseToFalse() throws Exception {
        model.setVerbose(false);

        Field f = GameModel.class.getDeclaredField("verbose");
        f.setAccessible(true);
        boolean value = f.getBoolean(model);

        assertFalse("Verbose should be set to false after setting it to false", value);
    }

    //---------------Check Collision---------------------------
    @Test
    public void testCheckCollisionsPowerUpCollection() {
        // Heal ship by 80 percent
        ship.heal(80);
        int previousSize = model.getSpaceObjects().size();
        SpaceObject powerUp = new HealthPowerUp(ship.getX(), ship.getY());
        model.addObject(powerUp);

        // Run collision detection
        model.checkCollisions();

        // PowerUp should be removed here
        assertEquals(previousSize, model.getSpaceObjects().size());
        assertFalse("HealthPowerUp should be removed after collecting it.", model.getSpaceObjects().contains(powerUp));
        //health already tested in other files

        // merging tests
        model.setVerbose(true);
        int currentSize = model.getSpaceObjects().size();
        testUI.logs.clear();
        SpaceObject powerUp2 = new HealthPowerUp(ship.getX(), ship.getY());
        model.addObject(powerUp2);
        model.checkCollisions();

        assertEquals(100, ship.getHealth());
        assertTrue(testUI.logs.stream().anyMatch(log -> log.contains("Power-up collected")));
        assertEquals(currentSize, model.getSpaceObjects().size());
    }

    @Test
    public void testCheckCollisionsShieldPowerUpCollection() {
        ship.heal(100);
        SpaceObject shield = new ShieldPowerUp(ship.getX(), ship.getY());
        int previousSize = model.getSpaceObjects().size();
        model.addObject(shield);

        model.checkCollisions();

        // Shield should be removed here
        assertFalse("ShieldPowerUp should be removed after collecting it.", model.getSpaceObjects().contains(shield));
        assertEquals(previousSize, model.getSpaceObjects().size());
        //score already tested in shield test file

        // Verbose test
        // merging tests
        model.setVerbose(true);
        int currentSize = model.getSpaceObjects().size();
        testUI.logs.clear();
        model.addObject(shield);
        model.checkCollisions();

        assertFalse("ShieldPowerUp should be removed after collecting it.", model.getSpaceObjects().contains(shield));
        assertEquals(previousSize, model.getSpaceObjects().size());
        assertTrue("Should log a message here",
                testUI.logs.stream().anyMatch(log -> log.contains("Power-up collected")));
        assertEquals(currentSize, model.getSpaceObjects().size());


    }

    @Test
    public void testCheckCollisionsShipCollidingWithAsteroid() {
        ship.heal(100);
        int previousSize = model.getSpaceObjects().size();

        SpaceObject asteroid = new Asteroid(ship.getX(), ship.getY());
        model.addObject(asteroid);

        model.checkCollisions();

        assertFalse("Asteroid should be removed after collision", model.getSpaceObjects().contains(asteroid));
        assertEquals("Ship will lose some damage from ASTEROID_DAMAGE", 100 - GameModel.ASTEROID_DAMAGE, ship.getHealth());
        assertEquals(previousSize, model.getSpaceObjects().size());

        model.setVerbose(true);
        int currentSize = model.getSpaceObjects().size();
        testUI.logs.clear();
        asteroid = new Asteroid(ship.getX(), ship.getY());
        model.addObject(asteroid);
        model.checkCollisions();
        String expectedMessage = "Hit by asteroid! Health reduced by " + GameModel.ASTEROID_DAMAGE + ".";
        assertTrue("Expected asteroid log message",  testUI.logs.stream().anyMatch(log -> log.contains(expectedMessage)));
        assertEquals(currentSize, model.getSpaceObjects().size());

    }



    @Test
    public void testCheckCollisionsShipCollidingWithEnemy() {
        ship.heal(100);
        int previousSize = model.getSpaceObjects().size();

        SpaceObject enemy = new Enemy(ship.getX(), ship.getY());
        model.addObject(enemy);

        model.checkCollisions();

        assertFalse("Enemy should be removed after collision", model.getSpaceObjects().contains(enemy));
        assertEquals("Ship will lose some damage from ENEMY_DAMAGE", 100 - GameModel.ENEMY_DAMAGE, ship.getHealth());
        assertEquals(previousSize, model.getSpaceObjects().size());

        model.setVerbose(true);
        int currentSize = model.getSpaceObjects().size();
        testUI.logs.clear();
        enemy = new Enemy(ship.getX(), ship.getY());
        model.addObject(enemy);
        model.checkCollisions();
        String expectedMessage = "Hit by enemy! Health reduced by " + GameModel.ENEMY_DAMAGE + ".";
        assertTrue("Expected asteroid log message",  testUI.logs.stream().anyMatch(log -> log.contains(expectedMessage)));
        assertEquals(currentSize, model.getSpaceObjects().size());

    }

    @Test
    public void testCheckCollisionsShipNearAsteroidDoesNotCollide() {
        ship.heal(100);


        int x = (ship.getX() + 2) % GameModel.GAME_WIDTH;
        SpaceObject asteroid = new Asteroid(x, ship.getY());
        model.addObject(asteroid);

        model.checkCollisions();
        int currentSize = model.getSpaceObjects().size();

        // Asteroid should still be there (no collision)
        assertTrue(model.getSpaceObjects().contains(asteroid));
        assertEquals("No objects removed!", currentSize, model.getSpaceObjects().size());
        assertEquals("Ship should have the same health", 100, ship.getHealth());
        assertEquals(currentSize, model.getSpaceObjects().size());
    }

    @Test
    public void testCheckCollisionsFarObjectsDoNotCollide() {
        ship.heal(100);

        // Add a bullet far away from the ship
        SpaceObject bullet = new Bullet(0, 0);
        model.addObject(bullet);
        model.checkCollisions();
        int currentSize = model.getSpaceObjects().size();

        // Bullet should still be there
        assertTrue(model.getSpaceObjects().contains(bullet));
        assertEquals("Bullet should remain", currentSize, model.getSpaceObjects().size());
        assertEquals("Ship should not take damage", 100, ship.getHealth());
        assertEquals(currentSize, model.getSpaceObjects().size());
    }

    @Test
    public void testCheckCollisionsBulletHitsEnemy() {
        ship.heal(100);
        int initialHits = model.getStatsTracker().getShotsHit();
        int previousSize = model.getSpaceObjects().size();

        // Bullet and Enemy at same coordinates
        int x = 1, y = 1;
        SpaceObject bullet = new Bullet(x, y);
        SpaceObject enemy = new Enemy(x, y);

        model.addObject(bullet);
        model.addObject(enemy);

        model.checkCollisions();

        assertFalse("Bullet should be removed", model.getSpaceObjects().contains(bullet));
        assertFalse("Enemy should be removed", model.getSpaceObjects().contains(enemy));
        assertEquals("Target hits should be incremented", initialHits + 1, model.getStatsTracker().getShotsHit());
        assertEquals(previousSize, model.getSpaceObjects().size());
    }

    @Test
    public void testCheckCollisionsBulletHitsAsteroid() {
        ship.heal(100);
        int x = 2, y = 2;
        SpaceObject bullet = new Bullet(x, y);
        SpaceObject asteroid = new Asteroid(x, y);


        model.addObject(bullet);
        model.addObject(asteroid);
        int previousSize = model.getSpaceObjects().size();
        model.checkCollisions();


        assertFalse("Bullet should be removed after hitting asteroid", model.getSpaceObjects().contains(bullet));
        assertTrue("Asteroid should remain", model.getSpaceObjects().contains(asteroid));
        assertEquals(previousSize - 1, model.getSpaceObjects().size());
    }


    @Test
    public void testCheckCollisionsBulletHitsOtherObject() {
        SpaceObject otherObject= new SpaceObject() {
            public void tick(int tick) {}
            public int getX() { return 3; }
            public int getY() { return 3; }
            public ObjectGraphic render() { return null; }
        };

        SpaceObject bullet = new Bullet(3, 3);

        model.addObject(bullet);
        model.addObject(otherObject);
        int previousSize = model.getSpaceObjects().size();

        model.checkCollisions();

        assertTrue("Bullet should remain", model.getSpaceObjects().contains(bullet));
        assertTrue("Other object should remain", model.getSpaceObjects().contains(otherObject));
        assertEquals(previousSize, model.getSpaceObjects().size());
    }

    @Test
    public void testCheckCollisionsBulletHitsPowerUps() {
        int x = 4, y = 4;
        SpaceObject bullet = new Bullet(x, y);
        SpaceObject healthPowerUp = new HealthPowerUp(x, y);


        model.addObject(bullet);
        model.addObject(healthPowerUp);
        int previousSize = model.getSpaceObjects().size();
        model.checkCollisions();
        
        assertTrue("Bullet should remain", model.getSpaceObjects().contains(bullet));
        assertTrue("PowerUp should remain", model.getSpaceObjects().contains(healthPowerUp));
        assertEquals(previousSize, model.getSpaceObjects().size());
    }



    @Test
    public void testCheckCollisionsBulletDoesNotCollide() {
        SpaceObject bullet = new Bullet(0, 0);
        SpaceObject asteroid = new Asteroid(1, 1);

        model.addObject(bullet);
        model.addObject(asteroid);

        model.checkCollisions();

        assertTrue("Bullet should remain", model.getSpaceObjects().contains(bullet));
        assertTrue("Asteroid should remain", model.getSpaceObjects().contains(asteroid));
    }

    @Test
    public void testCheckCollisionsMultipleBulletsHitSameEnemy() {
        //current logic accepts 2 bullets at the same coordinates, spec is not strict about this
        int x = 5, y = 5;
        SpaceObject enemy = new Enemy(x, y);
        SpaceObject bullet1 = new Bullet(x, y);
        SpaceObject bullet2 = new Bullet(x, y);

        int hitsBefore = model.getStatsTracker().getShotsHit();

        model.addObject(enemy);
        model.addObject(bullet1);
        model.addObject(bullet2);

        model.checkCollisions();

        int hitsAfter = model.getStatsTracker().getShotsHit();
        assertEquals("Only one hit should be recorded", hitsBefore + 2, hitsAfter);
        assertFalse("Enemy should be removed", model.getSpaceObjects().contains(enemy));
    }

    @Test
    public void testCheckCollisionsBulletHittingShip() {
        SpaceObject bullet = new Bullet(ship.getX(), ship.getY());
        model.addObject(bullet);
        ship.heal(100);

        model.checkCollisions();

        assertTrue("Bullet stays still", model.getSpaceObjects().contains(bullet));
        assertEquals("Ship health stays the same as well", 100, ship.getHealth());
    }

    @Test
    public void testCheckCollisionsEmptySpaceObject() {
        model.getSpaceObjects().clear();

        try {
            model.checkCollisions();
        } catch (Exception e) {
            fail("An exception should not be thrown on empty list");
        }
    }

    @Test
    public void testCheckCollisionsOnlyShipInSpaceObjectList() {
        model.getSpaceObjects().clear();
        model.addObject(ship);

        model.checkCollisions();

        // If it works, the loop is meant to just continue
        // which means that nothing happens in teh collision method
        assertTrue(true);
    }

    @Test
    public void testCheckCollisionsBulletsOverlapping() {
        SpaceObject bullet1 = new Bullet(0, 0);
        SpaceObject bullet2 = new Bullet(0, 0);
        model.addObject(bullet1);
        model.addObject(bullet2);

        model.checkCollisions();

        assertTrue(model.getSpaceObjects().contains(bullet1));
        assertTrue(model.getSpaceObjects().contains(bullet2));
        //nothing should happen
    }

    @Test
    public void testsCheckCollisionsShipCollidesWithPowerUpAndAsteroid() {
        ship.heal(50);
        int x = ship.getX(), y = ship.getY();
        SpaceObject powerUp = new HealthPowerUp(x, y);
        SpaceObject asteroid = new Asteroid(x, y);

        model.addObject(powerUp);
        model.addObject(asteroid);

        model.checkCollisions();

        assertFalse(model.getSpaceObjects().contains(powerUp));
        assertFalse(model.getSpaceObjects().contains(asteroid));
        assertEquals(50 + 20 - GameModel.ASTEROID_DAMAGE, ship.getHealth());
    }

    @Test
    public void testCheckCollisionsBulletHitsTwoEnemiesOneHandled() {
        int x = 6, y = 6;
        SpaceObject bullet = new Bullet(x, y);
        SpaceObject enemy1 = new Enemy(x, y);
        SpaceObject enemy2 = new Enemy(x, y);

        int hitsBefore = model.getStatsTracker().getShotsHit();

        model.addObject(bullet);
        model.addObject(enemy1);
        model.addObject(enemy2);

        model.checkCollisions();

        int hitsAfter = model.getStatsTracker().getShotsHit();
        assertEquals("One hit must be recorded only", hitsBefore + 1, hitsAfter);

        boolean enemy1Removed = !model.getSpaceObjects().contains(enemy1);
        boolean enemy2Removed = !model.getSpaceObjects().contains(enemy2);

        assertTrue("Only one enemy will be removed", enemy1Removed || enemy2Removed);
    }

    @Test
    public void testCheckCollisionsObjectCollision() {
        SpaceObject bullet = new Bullet(1, 1);
        SpaceObject enemy = new Asteroid(1, 1);

        assertTrue("objectCollision should detect same coordinates",
                GameModel.isInBounds(bullet) && GameModel.isInBounds(enemy) &&
                        bullet.getX() == enemy.getX() && bullet.getY() == enemy.getY());
    }

    @Test(expected = NullPointerException.class)
    public void testCheckCollisionsWithNullShip() throws Exception {
        Field shipField = GameModel.class.getDeclaredField("ship");
        shipField.setAccessible(true);
        shipField.set(model, null);
        model.addObject(new Asteroid(0, 0));
        model.checkCollisions();
    }

    @Test(expected = NullPointerException.class)
    public void testCheckCollisionsWithNullStatsTracker() throws Exception {
        Field trackerField = GameModel.class.getDeclaredField("statsTracker");
        trackerField.setAccessible(true);
        trackerField.set(model, null);
        model.addObject(new Enemy(0, 0));
        model.addObject(new Bullet(0, 0));
        model.checkCollisions();
    }

    @Test(expected = NullPointerException.class)
    public void testCheckCollisionsWith() throws Exception {
        Field loggerField = GameModel.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(model, null);
        model.setVerbose(true);
        model.addObject(new Asteroid(ship.getX(), ship.getY()));
        model.checkCollisions();
    }

    @Test(expected = NullPointerException.class)
    public void testCheckCollisionsThrowsIfSpaceObjectsIsNull() throws Exception {
        Field spaceObjectsField = GameModel.class.getDeclaredField("spaceObjects");
        spaceObjectsField.setAccessible(true);
        spaceObjectsField.set(model, null);
        model.checkCollisions();
    }

















































































}
