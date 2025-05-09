package game.core;

/**
 * Represents a movable and interactive object in the space game.
 */
public abstract class ObjectWithPosition implements SpaceObject {
    /**
     * The x coordinate of the Object
     */
    protected int x;
    /**
     * The y coordinate of the Object
     */
    protected int y;

    /**
     * Creates a movable and interactive object at the given coordinates.
     *
     * @param x the given x coordinate
     * @param y the given y coordinate
     */
    public ObjectWithPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    /**
     * Returns a string representation of the Object.
     * @ensures the class name and current position is returned as ClassName(x,y)
     * @return a string identifying the object's name and current position, eg. Bullet(2, 4)
     */
    @Override
    public String toString() {
        String nameAndPos = getClass().getName();
        String[] name = nameAndPos.split("\\.");
        return name[2] + "(" + x + ", " + y + ")";
    }
}
