package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.MessageFactory;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;


public class Player extends Publisher{
    private final String name;
    private Color playerColor;
    private final Station playerStation;

    /**
     * This constructor creates a player and his station
     * @param name player name
     */
    public Player(String name, PlayableCard initialCard, GoalCard privateGoalCard1, GoalCard privateGoalCard2){
        super();
        this.name = name;
        this.playerStation = new Station(this, initialCard, privateGoalCard1, privateGoalCard2);
    }

    @Override
    public void setMessageFactory(MessageFactory messageFactory) {
        super.setMessageFactory(messageFactory);
        this.playerStation.setMessageFactory(this.getMessageFactory());
    }

    /**
     * This method returns player's station
     * @return the station of the player
     */
    public Station getStation() {return this.playerStation; }

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
        this.getMessageFactory().sendMessageToAllGamePlayers(new AcceptedColorMessage(this.getName(),this.playerColor));
    }

    /**
     * This method returns the color chosen by the player
     * @return the Color chosen by the player
     */
    public Color getColor(){
        return this.playerColor;
    }

}
