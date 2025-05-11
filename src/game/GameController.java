package game;

import game.achievements.Achievement;
import game.achievements.AchievementManager;
import game.achievements.PlayerStatsTracker;
import game.core.SpaceObject;
import game.ui.UI;
import game.utility.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * The Controller handling the game flow and interactions.
 * <p>
 * Holds references to the UI and the Model, so it can pass information and references back and forth as necessary.<br>
 * Manages changes to the game, which are stored in the Model, and displayed by the UI.<br>
 *
 */
public class GameController {
    private final long startTime;
    private final UI ui;
    private final GameModel model;
    private final AchievementManager achievementManager;
    private boolean paused = false;

    private static final String FIRE = "F";
    private static final String PAUSE = "P";
    private static final String MOVE_UP = "W";
    private static final String MOVE_DOWN = "S";
    private static final String MOVE_LEFT = "A";
    private static final String MOVE_RIGHT = "D";


    /**
     * An internal variable indicating whether certain methods should log their actions.
     * Not all methods respect isVerbose.
     */
    private boolean isVerbose = false;


    /**
     * Initializes the game controller with the given UI, GameModel and AchievementManager.
     * Stores the UI, GameModel, AchievementManager and start time.
     * The start time System.currentTimeMillis() should be stored as a long.
     * Starts the UI using UI.start().
     *
     * @param ui the UI used to draw the Game
     * @param model the model used to maintain game information
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires model is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, GameModel model, AchievementManager achievementManager) {
        this.ui = ui;
        ui.start();
        this.model = model;
        this.startTime = System.currentTimeMillis(); // Current time
        this.achievementManager = achievementManager;
    }


    /**
     * Initializes the game controller with the given UI and GameModel.<br>
     * Stores the ui, model and start time.<br>
     * The start time System.currentTimeMillis() should be stored as a long.<br>
     *
     * @param ui the UI used to draw the Game
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, AchievementManager achievementManager) {
        this(ui, new GameModel(ui::log, new PlayerStatsTracker()), achievementManager);
    }

    /**
     * Starts the main game loop.<br>
     * <p>
     * Passes onTick and handlePlayerInput to ui.onStep and ui.onKey respectively.
     * @provided
     */
    public void startGame() {
        ui.onStep(this::onTick);
        ui.onKey(this::handlePlayerInput);
    }

    /**
     * Uses the provided tick to call and advance the following:<br>
     * - A call to model.updateGame(tick) to advance the game by the given tick.<br>
     * - A call to model.checkCollisions() to handle game interactions.<br>
     * - A call to model.spawnObjects() to handle object creation.<br>
     * - A call to model.levelUp() to check and handle leveling.<br>
     * - A call to refreshAchievements(tick) to handle achievement updating.<br>
     * - A call to renderGame() to draw the current state of the game.<br>
     * @param tick the provided tick
     * @provided
     */
    public void onTick(int tick) {
        model.updateGame(tick); // Update GameObjects
        model.checkCollisions(); // Check for Collisions
        model.spawnObjects(); // Handles new spawns
        model.levelUp(); // Level up when score threshold is met
        refreshAchievements(tick); // Handle achievement updating.
        renderGame(); // Update Visual

        // Check game over
        if (model.checkGameOver()) {
            pauseGame();
            showGameOverWindow();
        }
    }

    /**
     * Displays a Game Over window containing the player's final statistics and achievement
     * progress.<br>
     * <p>
     * This window includes:<br>
     * - Number of shots fired and shots hit<br>
     * - Number of Enemies destroyed<br>
     * - Survival time in seconds<br>
     * - Progress for each achievement, including name, description, completion percentage
     * and current tier<br>
     * @provided
     */
    private void showGameOverWindow() {

        // Create a new window to display game over stats.
        javax.swing.JFrame gameOverFrame = new javax.swing.JFrame("Game Over - Player Stats");
        gameOverFrame.setSize(400, 300);
        gameOverFrame.setLocationRelativeTo(null); // center on screen
        gameOverFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        StringBuilder sb = new StringBuilder();
        sb.append("Shots Fired: ").append(getStatsTracker().getShotsFired()).append("\n");
        sb.append("Shots Hit: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Enemies Destroyed: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Survival Time: ").append(
                getStatsTracker().getElapsedSeconds()).append(" seconds\n");


        List<Achievement> achievements = achievementManager.getAchievements();
        buildGameStats(achievements, sb);

        String statsText = sb.toString();

        // Create a text area to show stats.
        javax.swing.JTextArea statsArea = new javax.swing.JTextArea(statsText);
        statsArea.setEditable(false);
        statsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

        // Add the text area to a scroll pane (optional) and add it to the frame.
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(statsArea);
        gameOverFrame.add(scrollPane);

        // Make the window visible.
        gameOverFrame.setVisible(true);
    }

    /**
     * Build the game statistics in a string
     * @param achievements the achievements of the player
     * @param sb the string
     */
    private static void buildGameStats(List<Achievement> achievements, StringBuilder sb) {
        for (Achievement ach : achievements) {
            double progressPercent = ach.getProgress() * 100;
            sb.append(ach.getName())
                    .append(" - ")
                    .append(ach.getDescription())
                    .append(" (")
                    .append(String.format("%.0f%%", progressPercent))
                    .append(" complete, Tier: ")
                    .append(ach.getCurrentTier())
                    .append(")\n");
        }
    }

    /**
     * Renders the current game state, including score, health, and ship position.
     */
    public void renderGame() {
        List<SpaceObject> spaceObjectsAndShip = new ArrayList<>(model.getSpaceObjects());
        spaceObjectsAndShip.add(model.getShip());

        ui.setStat("Score", Integer.toString(model.getShip().getScore()));
        ui.setStat("Health", Integer.toString(model.getShip().getHealth()));
        ui.setStat("Level", Integer.toString(model.getLevel()));
        ui.setStat("Time Survived", (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        ui.render(spaceObjectsAndShip);
    }

    /**
     * Returns the current game model.
     * @return model which is the game
     */
    public GameModel getModel() {
        return this.model;
    }

    /**
     * Returns the current PlayerStatsTracker.
     * @return the current PlayerStatsTracker
     */
    public PlayerStatsTracker getStatsTracker() {
        return model.getStatsTracker();
    }


    /**
     * Sets verbose state to the provided input.
     * @requires model != null
     * @ensures this.isVerbose == verbose && model.verbose == verbose
     * @param verbose whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        this.isVerbose = verbose;
        model.setVerbose(verbose);
    }

    /**
     * Updates the player's progress towards achievements on every game tick, 
     * and uses the achievementManager to track and update the player's achievements.
     * Survivor mastered at 120 seconds
     * Enemy mastered at 20 shots
     * Sharp shooter achievement is determined via an accuracy based metric.
     * @requires model != null && achievementManager != null && ui != null
     * @ensures - survivorAchievement is set to  survival time / 120.0
     *          - enemyAchievement is set to hits / 20.0
     *          - sharpShooterAchievement is set to .0. if shots fired <= 10
     *                  otherwise to accuracy / 0.99 if shotsFired > 10
     *          - Store all new achievements that is mastered,
     * @param tick the tick
     */
    public void refreshAchievements(int tick) {
        double survivorAchievement
                = Math.min(1.0, model.getStatsTracker().getElapsedSeconds() / 120.0);
        double enemyAchievement
                = Math.min(1.0, model.getStatsTracker().getShotsHit() / 20.0);

        double sharpShooterAchievement;
        int shotsFired = model.getStatsTracker().getShotsFired();

        if (shotsFired <= 10) {
            sharpShooterAchievement = 0.0;
        } else {
            sharpShooterAchievement = Math.min(1.0, model.getStatsTracker().getAccuracy() / 0.99);
        }

        achievementManager.updateAchievement("Survivor", survivorAchievement);
        achievementManager.updateAchievement("Enemy Exterminator", enemyAchievement);
        achievementManager.updateAchievement("Sharp Shooter", sharpShooterAchievement);

        achievementManager.logAchievementMastered();

        ui.setStat("Survivor", String.format("%.2f", survivorAchievement));
        ui.setStat("Enemy Exterminator", String.format("%.2f", enemyAchievement));
        ui.setStat("Sharp Shooter", String.format("%.2f", sharpShooterAchievement));

        displayProgress(tick, survivorAchievement, enemyAchievement, sharpShooterAchievement);
    }

    /**
     *  This method Updates the UI statistics.
     * @param tick the tick
     * @param survivorAchievement the survivor achievement
     * @param enemyAchievement the enemy achievement
     * @param sharpShooterAchievement the sharp shoot achievement stat
     * @ensures  then updates the UI statistics
     *              with each new achievement's name and progress value.
     *          - If tick is divisible by 100 and verbose is true at that time,
     *              all achievement progress is then logged to the UI
     */
    private void displayProgress(int tick, double survivorAchievement,
                                 double enemyAchievement, double sharpShooterAchievement) {
        if (isVerbose && tick % 100 == 0) {
            ui.log("Survivor progress: " + String.format("%.2f", survivorAchievement));
            ui.log("Enemy Exterminator progress: "
                    + String.format("%.2f", enemyAchievement));
            ui.log("Sharp Shooter progress: " + String.format("%.2f", sharpShooterAchievement));
        }
    }

    /**
     * This method prints the ship moving through the UI.
     * @requires model != null && model.ship != null && ui != null
     * @ensures if isVerbose == true, then log the ship moving through ui
     */
    private void printShipMoving() {
        if (isVerbose) {
            ui.log("Ship moved to ("
                    + model.getShip().getX() + ", " + model.getShip().getY() + ")");
        }
    }

    /**
     * This checks whether an input string is valid.
     * @param input the input string from the user
     * @return true if input string is not null and exactly one character, false otherwise.
     */
    private static boolean validInput(String input) {
        return input != null && input.length() == 1;
    }

    /**
     * This method moves the ship and prints it as well if needed.
     * @requires model != null && model.ship != null && direction != null
     * @ensures that the ship has moved.
     * @param direction the direction on the grid
     */
    private void moveShip(Direction direction) {
        model.getShip().move(direction);
        model.checkCollisions();
        printShipMoving();
    }

    /**
     * Handles player input and performs actions such as moving the ship or firing Bullets.
     * For movement keys "W", "A", "S" and "D" the ship should be moved up, left, down,
     *      or right respectively, unless the game is paused.
     * For input "F" and "P", the appropriate methods are called.
     * @requires ui != null && model != null && model.ship != null && (input != null || input.length != 1)
     * @ensures - input is validated and action is taken upon that
     *          - if the game is paused, ignore all inputs that does not include "p" || "P".
     *          - if input is one of the movement keys, then move ship, check collison and print the ship moving.
     *          - if input is "F" || "f", then call fireBullet() and record shots fired.
     *          - if input is "P" || "p", pause the game
     *          - for other inputs, print log a message for invalid inputs.
     * @param input player input
     */
    public void handlePlayerInput(String input) {

        String invalidInput = "Invalid input. Use W, A, S, D, F, or P.";
        if (!validInput(input)) {
            ui.log(invalidInput);
            return;
        }
        String command = input.toUpperCase();

        if (paused && !command.equals(PAUSE)) {
            return;
        }
        
        switch (command) {
            case MOVE_UP -> moveShip(Direction.UP);
            case MOVE_LEFT -> moveShip(Direction.LEFT);
            case MOVE_DOWN -> moveShip(Direction.DOWN);
            case MOVE_RIGHT -> moveShip(Direction.RIGHT);
            case PAUSE -> pauseGame();

            case FIRE -> {
                model.fireBullet();
                model.getStatsTracker().recordShotFired();
            }
            default -> ui.log(invalidInput);
        }
    }

    /**
     * Calls ui.pause() to pause the game until the method is called again.
     * @requires ui != null && model != null
     * @ensures  - ui.pause() is called once, paused is toggled from its previous state.
     *           - A corresponding log message is printed depending on the state of paused.
     */
    public void pauseGame() {
        ui.pause();
        paused = !paused;

        if (paused) {
            String gamePaused = "Game paused.";
            ui.log(gamePaused);

        } else {
            // according to the spec must write this instead of game resumed.
            String resume = "Game unpaused.";
            ui.log(resume);
        }
    }

}

