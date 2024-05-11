package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This message is sent when player connects or reconnects to
 * a certain game and needs to be updated about game and player state
 */
public class OtherStationConfigurationMessage extends ConfigurationMessage {
    private final String nick;
    private final Color color;
    private final List<Tuple<Symbol,PlayableCardType>> cardsInHand;
    private final Map<Symbol, Integer> visibleSymbols;
    private final int numPoints;
    private final List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    public OtherStationConfigurationMessage(String nick, Color color, List<Tuple<Symbol,PlayableCardType>> cardsInHand, Map<Symbol, Integer> visibleSymbols,
                                            int numPoints, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.nick = nick;
        this.color = color;
        this.cardsInHand = cardsInHand;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.placedCardSequence = placedCardSequence;
    }

    /**
     * Getter for nickname of player owning that station
     * @return the nickname of station owner
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Returns the hashmap of visible symbols in station
     * @return the updated hashmap of visible symbols in station
     */
    public Map<Symbol, Integer> getVisibleSymbols() {
        return this.visibleSymbols;
    }

    /**
     * Getter for updated number of points associated to station
     * @return the number of points associated to station
     */
    public int getNumPoints() {
        return this.numPoints;
    }

    /**
     * Getter for station's color
     * @return the color associated to the station
     */
    public Color getColor() {
        return color;
    }

    /**
     * Getter for sequence of cards placed in station from the beginning (initial card)
     * @return the sequence of cards placed in station
     */
    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        return placedCardSequence;
    }

    /**
     * Getter for the cards in hand of another player. Only shows their seed, not the card!
     * @return a list of seeds corresponding to the cards in the hand of a specific player
     */
    public List<Tuple<Symbol, PlayableCardType>> getCardsInHand() {
        return cardsInHand;
    }


    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof ConfigurationMessageVisitor) ((ConfigurationMessageVisitor) visitor).visit(this);
    }
}