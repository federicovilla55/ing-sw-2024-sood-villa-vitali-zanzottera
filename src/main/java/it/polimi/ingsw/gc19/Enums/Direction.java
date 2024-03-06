package it.polimi.ingsw.gc19.Enums;

import java.util.EnumSet;

public enum Direction{
    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0), UP_LEFT(-1, 1), UP_RIGHT(1, 1), DOWN_RIGHT(1, -1), DOWN_LEFT(-1, -1);

    private final int x;
    private final int y;

    private Direction(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static EnumSet<Direction> getCornerDirection(){
        return EnumSet.of(UP_LEFT, UP_RIGHT,DOWN_LEFT, DOWN_RIGHT);
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

}
