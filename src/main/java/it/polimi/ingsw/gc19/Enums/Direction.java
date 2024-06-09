package it.polimi.ingsw.gc19.Enums;

/**
 * This enum represents the possible movement direction
 */
public enum Direction{

    UP_LEFT(CornerPosition.DOWN_RIGHT, -1, -1),
    UP_RIGHT(CornerPosition.DOWN_LEFT,-1, 1),
    DOWN_RIGHT(CornerPosition.UP_LEFT,1, 1),
    DOWN_LEFT(CornerPosition.UP_RIGHT,1, -1);

    /**
     * Represents the CornerPosition of the corner of this card encountered moving in direction specified
     */
    private final CornerPosition otherCornerPosition;

    /**
     * X-axis shift necessary to move in
     * the specified direction
     */
    private final int x;

    /**
     * Y-axis shift necessary to move in
     * the specified direction
     */
    private final int y;

    private Direction(CornerPosition otherCornerPosition, int x, int y){
        this.otherCornerPosition = otherCornerPosition;
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

    /**
     * This method return the position of other card's corner encountered moving from the card in the specified direction
     */
    public CornerPosition getOtherCornerPosition(){
        return this.otherCornerPosition;
    }

}
