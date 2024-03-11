package it.polimi.ingsw.gc19.Model.Enums;

import java.util.EnumSet;

public enum Direction{
    UP_LEFT(-1, 1), UP_RIGHT(1, 1), DOWN_RIGHT(1, -1), DOWN_LEFT(-1, -1);

    private final int x;
    private final int y;

    private Direction(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public CornerPosition getThisCornerPosition(){
        switch(this){
            case UP_LEFT -> {
                return CornerPosition.UP_LEFT;
            }
            case UP_RIGHT -> {
                return CornerPosition.UP_RIGHT;
            }
            case DOWN_LEFT -> {
                return CornerPosition.DOWN_LEFT;
            }
            case DOWN_RIGHT -> {
                return CornerPosition.DOWN_RIGHT;
            }
            default -> {
                throw new IllegalStateException();
            }
        }
    }

    public CornerPosition getOtherCornerPosition(){
        switch(this){
            case UP_LEFT -> {
                return CornerPosition.DOWN_RIGHT;
            }
            case UP_RIGHT -> {
                return CornerPosition.DOWN_LEFT;
            }
            case DOWN_LEFT -> {
                return CornerPosition.UP_RIGHT;
            }
            case DOWN_RIGHT -> {
                return CornerPosition.UP_LEFT;
            }
            default -> {
                throw new IllegalStateException();
            }
        }
    }

}
