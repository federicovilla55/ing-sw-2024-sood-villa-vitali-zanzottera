package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.MessageFactory;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;


/**
 * The Player class represents a player in the game.
 * Each player has a name, color, and a station.
 */
public class Player extends Publisher {
    /**
     * The name of the player.
     */
    private final String name;

    /**
     * The color chosen by the player.
     */
    private Color playerColor;

    /**
     * The station associated with the player.
     */
    private final Station playerStation;

    /**
     * Constructs a new player with the specified name, initial card and possible private goals.
     *
     * @param name             the name of the player
     * @param initialCard      the initial card for the player
     * @param privateGoalCard1 the first private goal card for the player
     * @param privateGoalCard2 the second private goal card for the player
     */
    public Player(String name, PlayableCard initialCard, GoalCard privateGoalCard1, GoalCard privateGoalCard2) {
        super();
        this.name = name;
        this.playerStation = new Station(this, initialCard, privateGoalCard1, privateGoalCard2);
    }

    /**
     * Sets the message factory for the player and its station.
     *
     * @param messageFactory the message factory to set
     */
    @Override
    public void setMessageFactory(MessageFactory messageFactory) {
        super.setMessageFactory(messageFactory);
        this.playerStation.setMessageFactory(this.getMessageFactory());
    }

    /**
     * This method returns player's station
     * @return the station of the player
     */
    public Station getStation() {
        return this.playerStation;
    }

    /**
     * This method returns player's name
     * @return the name of the player
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the color chosen by the player and notifies all game players.
     *
     * @param color the color to set for the player
     */
    public void setColor(Color color) {
        this.playerColor = color;
        this.getMessageFactory().sendMessageToAllGamePlayers(new AcceptedColorMessage(this.getName(), this.playerColor));
    }

    /**
     * This method returns the color chosen by the player
     * @return the Color chosen by the player
     */
    public Color getColor() {
        return this.playerColor;
    }
}
