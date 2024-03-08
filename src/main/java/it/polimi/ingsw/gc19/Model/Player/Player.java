package it.polimi.ingsw.gc19.Model.Player;

import it.polimi.ingsw.gc19.Model.Enums.Color;

import java.util.ArrayList;

public class Player {
    private final String name;
    private Color playerColor;
    public Player(String name)
    {
        this.name = name;
    }
    public String getName(){
        return  this.name;
    }
    
    // @todo: create a logic for asking the player
    // to select a color given a list of available colors
    public void setColor(Color color){
        this.playerColor = color;
    }

    public Color getColor(){
        return this.playerColor;
    }
}
