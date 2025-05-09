package game;

import game.achievements.PlayerStatsTracker;
import game.core.Ship;
import game.utility.Logger;
import game.ui.UI;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class GameModelTest {

    private GameModel model;
    private Ship ship;

    @Before
    public void setUp() {

        UI ui = new UI() {
            public void start() {}
            public void pause() {}
            public void stop() {}
            public void onStep(game.ui.Tickable tickable) {}
            public void onKey(game.ui.KeyHandler key) {}
            public void render(java.util.List<game.core.SpaceObject> objects) {}
            public void log(String message) {}
            public void setStat(String label, String value) {}
            public void logAchievementMastered(String message) {}
            public void logAchievements(java.util.List<game.achievements.Achievement> achievements) {}
            public void setAchievementProgressStat(String name, double progress) {}
        };

        Logger logger = ui::log;
        PlayerStatsTracker tracker = new PlayerStatsTracker(0L);
        model = new GameModel(logger, tracker);

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

    @Test
    public void testCheckGameOver_healthIsZero() {
        assertTrue(model.checkGameOver());
    }

    @Test
    public void testCheckGameOver_healthBelowZero() {
        ship.heal(-1);
        assertTrue(model.checkGameOver());
    }

    @Test
    public void testCheckGameOver_healthAboveZero() {
        ship.heal(5);
        assertFalse(model.checkGameOver());
    }
}
