package it.polimi.ingsw.gc19.Model.Enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum represents the possible movement direction
 */
public enum Direction{
    UP_LEFT(CornerPosition.UP_LEFT, CornerPosition.DOWN_RIGHT, -1, -1),
    UP_RIGHT(CornerPosition.UP_RIGHT, CornerPosition.DOWN_LEFT,-1, 1),
    DOWN_RIGHT(CornerPosition.DOWN_RIGHT, CornerPosition.UP_LEFT,1, 1),
    DOWN_LEFT(CornerPosition.DOWN_LEFT, CornerPosition.UP_RIGHT,1, -1);

    private final CornerPosition thisCornerPosition;
    private final CornerPosition otherCornerPosition;
    private final int x;
    private final int y;

    private Direction(CornerPosition thisCornerPosition, CornerPosition otherCornerPosition, int x, int y){
        this.thisCornerPosition = thisCornerPosition;
        this.otherCornerPosition = otherCornerPosition;
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    /**
     * This method return the position of this card's corner encountered moving from the card in the specified direction
     */
    public CornerPosition getThisCornerPosition(){
        return this.thisCornerPosition;
    }

    /**
     * This method return the position of other card's corner encountered moving from the card in the specified direction
     */
    public CornerPosition getOtherCornerPosition(){
        return this.otherCornerPosition;
    }

}
