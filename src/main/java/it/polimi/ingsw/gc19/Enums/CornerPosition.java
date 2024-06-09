package it.polimi.ingsw.gc19.Enums;

/**
 * This enum represents the position inside the 2x2 matrix of the card
 */
public enum CornerPosition{
    UP_LEFT(0, 0), UP_RIGHT(0, 1), DOWN_RIGHT(1, 1), DOWN_LEFT(1, 0);

    /**
     * X coordinate of corner in a 2x2 matrix with
     * {@link #UP_LEFT} in <code>(0, 0)</code>
     */
    private final int x;

    /**
     * Y coordinate of corner in a 2x2 matrix with
     * {@link #UP_LEFT} in <code>(0, 0)</code>
     */
    private final int y;

    private CornerPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Getter for {@link #x}
     * @return {@link #x} value
     */
    public int getX(){
        return this.x;
    }

    /**
     * Getter for {@link #y}
     * @return {@link #y} value
     */
    public int getY(){
        return this.y;
    }
}