package game.achievements;

/**
 * Represents a single achievement with progress tracking and tier information.
 */
public interface Achievement {
    
    /**
     * A method to get the name of the achievement.
     * @return the unique name of the achievement.
     */
    String getName();

    /**
     * A method to get a description of the achievement.
     * @return a description of the achievement.
     */
    String getDescription();

    /**
     * This returns the current progress.
     * @ensures 0.0 <= getProgress() <= 1.0
     * @return the current progress as a double between 0.0 (0%) and 1.0 (100%).
     */
    double getProgress();

    /**
     * Sets the progress to the specified value.
     * @param newProgress the new progress
     * @Requires newProgress is between 0.0 and 1.0, inclusive.
     * @ensures getProgress() == newProgress, getProgress() <= 1.0 after the update 
     * (i.e., progress is capped at 1.0)., getProgress() >= 0.0 after the update. 
     */
    void setProgress(double newProgress);

    /**
     * Returns "Novice" if getProgress() < 0.5, 
     * "Expert" if 0.5 <= getProgress() < 0.999, 
     * and "Master" if getProgress() >=0.999.
     * @return a string representing the current tier (e.g., "Novice", "Expert", "Master") based on the progress.
     */
    String getCurrentTier();
}
