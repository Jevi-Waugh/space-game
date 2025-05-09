package game;

import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.utility.Logger;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game information and state. Stores and manipulates the game state.
 */
public class GameModel {
    public static final int GAME_HEIGHT = 20;
    public static final int GAME_WIDTH = 10;
    public static final int START_SPAWN_RATE = 2; // spawn rate (percentage chance per tick)
    public static final int SPAWN_RATE_INCREASE = 5; // Increase spawn rate by 5% per level
    public static final int START_LEVEL = 1; // Starting level value
    public static final int SCORE_THRESHOLD = 100; // Score threshold for leveling
    public static final int ASTEROID_DAMAGE = 10; // The amount of damage an asteroid deals
    public static final int ENEMY_DAMAGE = 20; // The amount of damage an enemy deals
    public static final double ENEMY_SPAWN_RATE = 0.5; // Percentage of asteroid spawn chance
    public static final double POWER_UP_SPAWN_RATE = 0.25; // Percentage of asteroid spawn chance

    private final Random random = new Random(); // ONLY USED IN this.spawnObjects()
    private final List<SpaceObject> spaceObjects; // List of all objects
    private final Ship ship; // Core.Ship starts at (5, 10) with 100 health
    private int lvl; // The current game level
    private int spawnRate; // The current game spawn rate
    private final Logger logger; // The Logger reference used for logging.
    
    private boolean verbose;
    private final PlayerStatsTracker statsTracker;

    
    /**
     * Constructs a new GameModel that manages the state of the space game.
     * 
     * This constructor:
     * - Instantiates an empty list to store all SpaceObjects.
     * - Instantiates the game level with the starting level value.
     * - Instantiates the game spawn rate with the starting spawn rate.
     * - Instantiates a new Ship.
     * - Stores reference to the given Logger.
     * - Stores reference to the given PlayerStatsTracker.
     * 
     * The logger should be a method reference to a log method, such as UI::log.
     * Example: GameModel model = new GameModel(ui::log, new PlayerStatsTracker());
     * 
     * @param logger a functional interface for passing information between classes.
     * @param statsTracker a PlayerStatsTracker instance to record stats.
     *
     * @requires logger != null && statsTracker != null
     * @throws NullPointerException if logger or statsTracker is null
     */
    public GameModel(Logger logger, PlayerStatsTracker statsTracker) {
        spaceObjects = new ArrayList<>();
        lvl = START_LEVEL;
        spawnRate = START_SPAWN_RATE;
        ship = new Ship();
        this.logger = logger;
        this.statsTracker = statsTracker;
    }

    /**
     * Returns the ship instance in the game.
     *
     * @return the current ship instance.
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns a list of all SpaceObjects in the game.
     *
     * @return a list of all spaceObjects.
     */
    public List<SpaceObject> getSpaceObjects() {
        return spaceObjects;
    }

    /**
     * Returns the current level.
     *
     * @return the current level.
     */
    public int getLevel() {
        return lvl;
    }

    /**
     * Returns the current player stats tracker.
     * @return the current player stats tracker.
     */
    public PlayerStatsTracker getStatsTracker() {
        return this.statsTracker;
    }

    /**
     * Adds a SpaceObject to the game
     * Objects are considered part of the game only when they are tracked by the model.
     *
     * @param object the SpaceObject to be added to the game.
     * @requires object != null.
     */
    public void addObject(SpaceObject object) {
        this.spaceObjects.add(object);
    }

    /**
     * This method checks if a specific coordinate is free
     * @param x the x-axis parameter
     * @param y the y-axis parameter
     * @return true if the coordinate is free and vice versa
     * @requires 0 <= x < GAME_WIDTH
     *        && 0 <= Y < GAME_HEIGHT
     *        && ship != null
     *        && spaceObjects != null
     * @ensures that the result is true if no ship nor spaceObject is present at x and y
     */
    private boolean coordinateFree(int x, int y) {
        if (ship.getX() == x && ship.getY() == y) {
            return false;
        }
        for (SpaceObject spaceObject : spaceObjects) {
            if (spaceObject.getX() == x && spaceObject.getY() == y) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves all objects and updates the game state.
     * @param tick - the tick value passed through to the objects tick() method.
     * @requires spaceObjects != null
     * @ensures all space objects are updated and only in-bound objects remain
     */
    public void updateGame(int tick) {

        Iterator<SpaceObject> iterator = spaceObjects.iterator();
        while (iterator.hasNext()) {
            SpaceObject spaceObject = iterator.next();
            // Objects should be moved by calling
            // .tick(tick) on each object.
            spaceObject.tick(tick);

            // The game state is updated
            // by removing out-of-bound objects during the tick
            if (!isInBounds(spaceObject)) { 
                iterator.remove();
            }
        }
    }
    
    /**
     * Sets verbose state to the provided input.
     * @param verbose set true to enable verbose and vice versa
     * @ensures this.verbose == verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    /**
     * Spawns new objects (Asteroids, Enemies, and PowerUp) at random positions.
     * Uses this.random to make EXACTLY 6 calls to random.nextInt() and 1 random.nextBoolean.
     *
     1. Check if an Asteroid should spawn (random.nextInt(100) < spawnRate)
     2. If spawning an Asteroid, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     3. Check if an Enemy should spawn (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE)
     4. If spawning an Enemy, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     5. Check if a PowerUp should spawn (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE)
     6. If spawning a PowerUp, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     7. If spawning a PowerUp, spawn a ShieldPowerUp if random.nextBoolean(), else a HealthPowerUp.
     *
     * @requires random != null && ship != null && spaceObjects != null
     * @ensures - All random calls are made in the correct order
     *          - Any object that is spawned will appear at y = 0
     *          - A new space object is added to spaceObjects iff it passes the specified
     *            spawn condition and the coordinate is free.
     */
    public void spawnObjects() {

        // Spawn asteroids with a chance determined by spawnRate
        if (random.nextInt(100) < spawnRate) {
            int x = random.nextInt(GAME_WIDTH); // Random x-coordinate
            int y = 0; // Spawn at the top of the screen
            if (coordinateFree(x, y)) {
                spaceObjects.add(new Asteroid(x, y));
            }
        }

        // Spawn enemies with a lower chance
        // Half the rate of asteroids
        if (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            if (coordinateFree(x, y)) {
                spaceObjects.add(new Enemy(x, y));
            }
        }

        // Spawn power-ups with an even lower chance
        // One-fourth the spawn rate of asteroids
        if (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            PowerUp powerUp = random.nextBoolean() ? new ShieldPowerUp(x, y) :
                    new HealthPowerUp(x, y);
            if (coordinateFree(x, y)) {
                spaceObjects.add(powerUp);
            }
        }
    }

    /**
     * A generic method to check if 2 space objects are colliding
     * by checking their relative x and y-axis coordinates.
     * @param spaceObject the current spaceObject
     * @param otherSpaceObject the other spaceObject that we will be checking against
     * @return true if both space objects are on the same coordinates and false otherwise
     * @requires spaceObject != null && otherSpaceObject != null
     * @ensures the result is true iff spaceObject and otherSpaceObject
     *          have equal x and y-axis values
     */
    private static boolean objectCollision(SpaceObject spaceObject, SpaceObject otherSpaceObject) {
        // changed so that method is not just restricted to checking against ships.
        return (spaceObject.getX() == otherSpaceObject.getX())
                && (spaceObject.getY() == otherSpaceObject.getY());
    }

    /**
     * This method increments the level of the player
     * If level progression requirements are satisfied,
     *      - the score must not be less than the current level multiplied
     *        by the score threshold.
     *      - levels up the game by increasing the spawn rate and level number by 1.
     * If the level is increased, and verbose is set to true,
     *      the following is logged: "Level Up! Welcome to Level {new level}.
     *      Spawn rate increased to {new spawn rate}%."
     * @requires ship != null && logger != null
     * @ensures - if ship's score >= (lvl * SCORE_THRESHOLD),
     *            then increment lvl and spawnRate by SPAWN_RATE_INCREASE
     *          - if verbose == true, then log the level up message
     *
     * levels up the game by increasing the spawn rate and level number.
     */
    public void levelUp() {
        if (ship.getScore() >= (lvl * SCORE_THRESHOLD)) {
            // Increase the level by 1
            this.lvl++;
            // increase spawn rate by the increase
            spawnRate += SPAWN_RATE_INCREASE;

            if (verbose) {
                logger.log("Level Up! Welcome to Level "
                        + lvl + ". Spawn rate increased to " + spawnRate + "%.");
            }
        }
    }

    /**
     * Fires a Bullet from the ship's current position.
     * @requires ship != null
     * @ensures A new Bullet is created at (ship.getX(), ship.getY())
     *          and added to spaceObjects.
     */
    public void fireBullet() {
        
        int bulletX = ship.getX();
        int bulletY = ship.getY();
        // Core.Bullet starts just above the ship
        spaceObjects.add(new Bullet(bulletX, bulletY));
        // spec does not say to do the following
        // statsTracker.recordShotFired();
        // logger.log("Core.Bullet fired!");

    }

    /**
     * PowerUp collision is handled
     * @requires spaceObject != null
     * @param powerUp the type of PowerUp
     * @param spaceObject the collided PowerUp
     */
    private void powerUpCollection(PowerUp powerUp, SpaceObject spaceObject) {
        powerUp.applyEffect(ship);
        if (this.verbose) {
            logger.log("Power-up collected: " + spaceObject.render());
        }
    }

    /**
     * Handles Asteroid collisions
     * @required ship != null && ASTEROID_DAMAGE > 0
     * @ensures ship takes damage and logs a message if verbose is true
     */
    private void handleAsteroidCollision() {
        ship.takeDamage(ASTEROID_DAMAGE);
        if (this.verbose) {
            logger.log("Hit by asteroid! Health reduced by " + ASTEROID_DAMAGE + ".");
        }
    }

    /**
     * Handles Enemy collisions
     * @required ship != null && ENEMY_DAMAGE > 0
     * @ensures ship takes damage and logs a message if verbose is true
     */
    private void handleEnemyCollision() {
        ship.takeDamage(ENEMY_DAMAGE);
        if (this.verbose) {
            logger.log("Hit by enemy! Health reduced by " + ENEMY_DAMAGE + ".");
        }
    }

    /**
     * Handles Enemy collisions with the given space objects
     * @param toRemove the list that contains spaceObjects to be removed
     * @param bullet the bullet
     * @param enemy the enemy
     * @requires
     */
    private void handleEnemyCollision(List<SpaceObject> toRemove, SpaceObject bullet,
                                      SpaceObject enemy) {
        toRemove.add(bullet);  // Remove bullet
        toRemove.add(enemy); // Remove enemy
        // Also, record the shot hit using recordShotHit() to track successful hits.
        this.statsTracker.recordShotHit();
    }

    /**
     * Check ship collision
     *      - If the ship is colliding with a PowerUp, apply the effect,
     *        and if verbose is true, log "PowerUp collected: {obj.render()}"
     *      - If the ship is colliding with an Asteroid or Enemy, take the appropriate damage,
     *        and if verbose is true, log "Hit by {obj.render()}! Health reduced by {damage_taken}."
     *      - For any collisions with the Ship, the colliding object should be removed.
     * @param toRemove the list that has spaceObjects to be removed
     * @param spaceObject the collided spaceObject
     */
    private void shipCollision(List<SpaceObject> toRemove, SpaceObject spaceObject) {

        switch (spaceObject) {
            case PowerUp powerUp -> powerUpCollection(powerUp, spaceObject);
            case Asteroid asteroid -> handleAsteroidCollision();
            case Enemy enemy -> handleEnemyCollision();
            default -> { }
        }
        toRemove.add(spaceObject);
    }

    /**
     * Check and handle bullet collision
     *      - If a Bullet collides with an Enemy, remove both the Enemy and the Bullet. No logging required.
     *      - record the shot hit using recordShotHit() to track successful hits.
     *              and, recordShotHit() is only called when a Bullet successfully hits an Enemy.
     *      - If a Bullet collides with an Asteroid, remove just the Bullet. No logging required.
     * @param toRemove the list that has spaceObjects to be removed
     * @param bullet  the collided spaceObject
     */
    private void bulletCollision(List<SpaceObject> toRemove, SpaceObject bullet) {
        for (SpaceObject spaceObject : spaceObjects) {
            // Check only Enemies and asteroid
            if (spaceObject == bullet) {
                continue;
            }

            if (objectCollision(bullet, spaceObject)) {
                switch (spaceObject) {
                    // using enemy here instead of spaceObject in param of func
                    case Enemy enemy -> handleEnemyCollision(toRemove, bullet, enemy);
                    case Asteroid asteroid -> toRemove.add(bullet);
                    default -> { }
                }
            }
        }
    }

    /**
     * Detects and handles collisions between spaceObjects (Ship and Bullet collisions).
     * @requires SpaceObject != null && ship != null && statsTracker != null && logger != null
     * @ensures - collisions are detected through the objectCollision method and
     *          - collisions are resolved according to spaceObject type and results in healing or taken damage.
     *          - For any spaceObject colliding with the ship, the colliding spaceObject is removed
     *          - if verbose == true then, log a message corresponding to the collision and other spaceObject.
     *          - if a Bullet collides with an Enemy, then the Bullet and Enemy are both removed,
     *                  record the shot through the statsTracker
     *          - if a Bullet collides with an Asteroid, the Bullet is removed.
     */
    public void checkCollisions() {
        List<SpaceObject> toRemove = new ArrayList<>();

        for (SpaceObject spaceObject : spaceObjects) {

            // Skip checking Ships (No ships should be in this list)
            if (spaceObject instanceof Ship) {
                continue;
            }

            // Check and handle Ship collision
            if (objectCollision(ship, spaceObject) && !(spaceObject instanceof Bullet)) {
                shipCollision(toRemove, spaceObject);
            }
            //Check and handle Bullet Collision
            if ((spaceObject instanceof Bullet)) {
                bulletCollision(toRemove, spaceObject);
            }
        }
        spaceObjects.removeAll(toRemove); // Remove all collided objects
    }


    /**
     * Sets the seed of the Random instance created in the constructor using .setSeed().<br>
     * <p>
     * This method should NEVER be called.
     *
     * @param seed to be set for the Random instance
     * @provided
     */
    public void setRandomSeed(int seed) {
        this.random.setSeed(seed);
    }

    /**
     * Checks if the game is over.
     * @return true if the Ship health is either zero or less, false otherwise
     * @requires ship != null
     * @ensures the result is true if ship.getHealth <= 0 and vice versa
     */
    public boolean checkGameOver() {
        return ship.getHealth() <= 0;
    }

    /**
     *  Checks if the given SpaceObject is inside the game bounds.
     * @param spaceObject - the SpaceObject to check
     * @return true if the SpaceObject is in bounds, false otherwise
     * @requires spaceObject != null
     * @ensures that the result is true if 0 <= spaceObject.getX() <= GAME_WIDTH
     *          && 0 <= spaceObject.getY() <= GAME_HEIGHT
     */
    public static boolean isInBounds(SpaceObject spaceObject) {
        return spaceObject != null && spaceObject.getX() >= 0
                && spaceObject.getX() < GAME_WIDTH
                && spaceObject.getY() >= 0
                && spaceObject.getY() < GAME_HEIGHT;

    }
   
    
}
