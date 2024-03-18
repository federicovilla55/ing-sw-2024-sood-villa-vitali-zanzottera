package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Station.Station;

public class Player {
    private final String name;
    private Color playerColor;
    private final Station playerStation;

    /**
     * This constructor creates a player and his station
     * @param name player name
     */
    public Player(String name, PlayableCard initialCard, GoalCard privateGoalCard1, GoalCard privateGoalCard2){
        this.name = name;
        this.playerStation = new Station(initialCard, privateGoalCard1, privateGoalCard2);
    }

    /**
     * This method returns player's station
     * @return the station of the player
     */
    public Station getPlayerStation() {return this.playerStation; }

    /**
     * This method returns player's name
     * @return the name of the player
     */
    public String getName(){
        return  this.name;
    }

    /**
     * This method set player color
     */
    public void setColor(Color color){
        this.playerColor = color;
    }

    /**
     * This method returns the color chosen by the player
     * @return the Color chosen by the player
     */
    public Color getColor(){
        return this.playerColor;
    }


}
