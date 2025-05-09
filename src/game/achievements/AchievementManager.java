package game.achievements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GameAchievementManager coordinates achievement updates, file persistence management.
 */
public class AchievementManager {

    private final AchievementFile achievementFile;

    // Stores the name as the key
    private final Map<String, Achievement> achievementMap;

    // ensures that it cannot be re-assigned or anything.
    private final Set<String> loggedAchievements = new HashSet<>();
    
    /**
     * Constructs a GameAchievementManager with the specified AchievementFile.
     * @param achievementFile the AchievementFile instance to use (non-null)
     */
    public AchievementManager(AchievementFile achievementFile) {
        if (achievementFile == null) {
            throw new IllegalArgumentException("Achievement is null");
        } 
        this.achievementFile = achievementFile;
        this.achievementMap = new HashMap<>();
    }

    /**
     * Registers a new achievement.
     * @param achievement the Achievement to register.
     */
    public void addAchievement(Achievement achievement) {
        //defensive programming
        if (achievement == null) {
            throw new IllegalArgumentException("achievement is null");
        }

        // stores the achievement with its name as the key in a hashmap
        this.achievementMap.put(achievement.getName(), achievement);
    }

    /**
     * Sets the progress of the specified achievement to a given amount.
     * @param achievementName the name of the achievement.
     * @param absoluteProgressValue the value the achievement's progress will be set to.
     */
    public void updateAchievement(String achievementName,  double absoluteProgressValue) {
       
        // find the achievement object
        Achievement achievement = this.achievementMap.get(achievementName);
        if (achievement == null) {
            throw new IllegalArgumentException(
                    "No achievement is registered under" + achievementName);
        }
        // Change progress to passed in value
        achievement.setProgress(absoluteProgressValue);
    }

    /**
     * Checks all registered achievements. For any achievement that is 
     * mastered and has not yet been logged, this method logs the event 
     * via AchievementFile, and marks the achievement as logged.
     */
    public void logAchievementMastered() {
        for (Achievement achievement : this.achievementMap.values()) {
            if (achievement.getCurrentTier().equals("Master")
                    && !loggedAchievements.contains(achievement.getName())) {
                // Mark as logged
                loggedAchievements.add(achievement.getName());
                
                // log via AchieveemntFile
                this.achievementFile.save(achievement.getName());
            }
        }
    }

    /**
     * Returns a list of all registered achievements.
     * @return a List of Achievement objects.
     */
    public List<Achievement> getAchievements() {
        // passes in the achievement values and returns an arraylist
        return new ArrayList<>(this.achievementMap.values());
    }
}
