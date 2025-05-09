package game.achievements;

/**
 * Represents a tracker for player statistics.
 * This class monitors the number and accuracy of shots the player has fired since the game start time.
 */
public class PlayerStatsTracker {
    // private double accuracy;
    private int shotsFired;
    private int targetHit;
    private final long startTime;


    /**
     * Constructs a PlayerStatsTracker with a custom start time.
     * @param startTime - the time when the tracking began
     */
    public PlayerStatsTracker(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Constructs a PlayerStatsTracker with current system time (in milliseconds) as the start time.
     */
    public PlayerStatsTracker() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Records the player firing one shot, by incrementing shots fired by 1.
     */
    public void recordShotFired() {
        this.shotsFired++;
    }
    
    /**
     * Records the player hitting one target, by incrementing shots hit by 1.
     */
    public void recordShotHit() {
        this.targetHit++;

    }

    /**
     * Returns the total number of shots that the player has fired.
     * @return the number of shots fired
     */
    public int getShotsFired() {
        return this.shotsFired;
    }
    
    /**
     * returns the total number of shots the player has successfully hit.
     * @return the number of shots hit
     */
    public int getShotsHit() {
        return this.targetHit;

    }

    /**
     * Returns the number of seconds elapsed since the tracker started.
     * @return the elapsed time in seconds
     */
    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * Returns the player's shooting accuracy percentage. 
     * Accuracy is calculated as shots hit divided by shots fired.
     * Unless no shots are fired, in which case accuracy is 0.0.
     * @return the shooting average percentage as a decimal.
     */
    public double getAccuracy() {
        if (this.shotsFired == 0) {
            //Unless no shots are fired, in which case accuracy is 0.0.
            return 0.0;
        } else {
            return (double) this.targetHit / this.shotsFired;
            
        }
    }

}
