package game.achievements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of AchievementFile using standard file I/O.
 */
public class FileHandler implements AchievementFile {

    private String fileLocation  = DEFAULT_LOCATION;

    /**
     * Default constructor
     */
    public FileHandler() {  }
    
    /**
     * Sets the file location to save to.
     * @param fileLocation new file location
     */
    @Override
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    /**Gets the location currently being saved to.
     * @return the file locatation
     */
    @Override
    public String getFileLocation() {
        return this.fileLocation;
    }

    /**
     * Saves the given data to a file followed by a new-line character.
     * @param data - the data to be saved.
     */
    @Override
    public void save(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileLocation))) {
            writer.write(data + "\n");
            // or  writer.newLine();
        } catch (IOException error) {
            //Spec says nothing about throwing errors
        }
    }
    
    /**
     * Loads and returns all previously saved data as a list of strings.
     * @return a list of saved data entries.
     */
    @Override
    public List<String> read() {
        List<String> dataList = new ArrayList<>();
        // try with resources just like in prac
        try (BufferedReader reader = new BufferedReader(new FileReader(this.fileLocation))) {
            // try-with-resourses ensures things are closed
            String line;
            while ((line = reader.readLine()) != null) {
                //Remove white space
                dataList.add(line.trim());
            }

        } catch (IOException error) {
            System.err.println("Error reading file: " + error.getMessage());
        }
        return dataList;
    }
}
