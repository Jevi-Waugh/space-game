package game.achievements;

/**
 * A concrete implementation of the Achievement interface.
 */
public class GameAchievement implements Achievement {

    private final String name;
    private final String description;
    // Progress is tracked as a value between 0.0 and 1.0.
    private double progress;

    /**
     * Constructs an Achievement with the specified name and description. The initial progress is 0.
     * @param name - the unique name.
     * @param description - the achievement description.
     */
    public GameAchievement(String name, String description) {
        // defensive programming
        // remember to use requires
        if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("The name and description should "
                    + "not be null nor empty");
        }
        this.name = name;
        this.description = description;
        // The initial progress is 0.
        this.progress = 0;
    }

    /** This will return the unique name of the achievement.
     * @return the unique name of the achievement.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**  will return a description of the achievement.
     * @return a description of the achievement.
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /** This will return the current progress
     * @return the current progress as a double between 0.0 (0%) and 1.0 (100%).
     */
    @Override
    public double getProgress() {
        return this.progress;
    }

    /**
     * Sets the progress to the specified value.
     * @param newProgress - the updated progress.
     */
    @Override
    public void setProgress(double newProgress) {
        // remember the interface has a requirement so we make sure to satisfy here.
        this.progress = Math.max(0.0, Math.min(1.0, newProgress));
    }

    /**
     * Returns "Novice" if getProgress() < 0.5, "Expert" if 0.5 <= getProgress() < 0.999, and "Master" if getProgress() >=0.999.
     * @return a string representing the current tier (e.g., "Novice", "Expert", "Master") based on the progress.
     */
    @Override
    public String getCurrentTier() {

        if (this.progress < 0.5) {
            return "Novice";
        } else if (this.progress < 0.999) {
            return "Expert";
        } else {
            return "Master";
        }
    }

}
