package it.polimi.ingsw.gc19.Model.Enums;

public enum CornerPosition{
    UP_LEFT(0, 0), UP_RIGHT(0, 1), DOWN_RIGHT(1, 1), DOWN_LEFT(1, 0);

    private final int x;
    private final int y;

    private CornerPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }
}
