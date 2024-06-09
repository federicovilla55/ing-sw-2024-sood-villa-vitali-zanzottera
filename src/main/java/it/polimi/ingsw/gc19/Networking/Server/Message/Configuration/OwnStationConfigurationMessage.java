package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;
import it.polimi.ingsw.gc19.Enums.GameState;

import java.util.List;
import java.util.Map;

/**
 * This message is sent for updating a specified player about
 * his own station after reconnection
 */
public class OwnStationConfigurationMessage extends ConfigurationMessage {

    /**
     * Nickname of the owner player
     */
    private final String nick;

    /**
     * {@link Color} chosen by the player, otherwise <code>null</code>
     */
    private final Color color;

    /**
     * List of infos about cards in hand
     */
    private final List<PlayableCard> cardsInHand;

    /**
     * Visible symbols in player's {@link Station}
     */
    private final Map<Symbol, Integer> visibleSymbols;

    /**
     * Chosen goal card. If player hasn't still chosen
     * his private goal card, this attribute is null
     */
    private final GoalCard privateGoalCard;

    /**
     * Number of points of the player
     */
    private final int numPoints;

    /**
     * Initial card if already chosen, otherwise <code>null</code>
     */
    private final PlayableCard initialCard;

    /**
     * First goal card that user can choose during {@link GameState#SETUP}
     */
    private final GoalCard goalCard1;

    /**
     * Second goal card that user can choose during {@link GameState#SETUP}
     */
    private final GoalCard goalCard2;

    /**
     * Sequence of cards placed by the player. For each card, there
     * is its position on the grid.
     */
    private final List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence;

    public OwnStationConfigurationMessage(String nick, Color color, List<PlayableCard> cardsInHand, Map<Symbol, Integer> visibleSymbols, GoalCard privateGoalCard, int numPoints,
                                          PlayableCard initialCard, GoalCard goalCard1, GoalCard goalCard2, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.nick = nick;
        this.color = color;
        this.cardsInHand = cardsInHand;
        this.visibleSymbols = visibleSymbols;
        this.privateGoalCard = privateGoalCard;
        this.numPoints = numPoints;
        this.initialCard = initialCard;
        this.goalCard1 = goalCard1;
        this.goalCard2 = goalCard2;
        this.placedCardSequence = placedCardSequence;
    }

    /**
     * Getter for station's owner nickname
     * @return the nickname of player owner of the station
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Getter for hashmap of visible symbols in station
     * @return the updated hashmap of visible symbols in station
     */
    public Map<Symbol, Integer> getVisibleSymbols() {
        return this.visibleSymbols;
    }

    /**
     * Getter for number of points
     * @return the number of point of the current player
     */
    public int getNumPoints() {
        return this.numPoints;
    }

    /**
     * Getter for initial card associated to the station
     * @return the initial card associated to the station
     */
    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

    /**
     * Getter for first goal that can be chosen
     * @return the first goal card player can choose
     */
    public GoalCard getGoalCard1() {
        return this.goalCard1;
    }

    /**
     * Getter for second gol card that can be chosen by the player
     * @return the second goal card player can choose
     */
    public GoalCard getGoalCard2() {
        return this.goalCard2;
    }

    /**
     * Getter for color chosen by player
     * @return the color chosen by player
     */
    public Color getColor() {
        return color;
    }

    /**
     * Getter for list of {@link PlayableCard} visible in station owned
     * by the player
     * @return list of {@link PlayableCard} owned by player
     */
    public List<PlayableCard> getCardsInHand() {
        return cardsInHand;
    }

    /**
     * Getter for private goal card chosen by station owner; returns
     * null if player hasn't chosen his private goal card
     * @return the {@link GoalCard} chosen by the player
     */
    public GoalCard getPrivateGoalCard() {
        return privateGoalCard;
    }

    /**
     * Getter for ordered sequence of cards (from the beginning of the game)
     * placed by the owner station
     * @return the ordered sequence of {@link PlayableCard} placed by the player
     */
    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        return placedCardSequence;
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