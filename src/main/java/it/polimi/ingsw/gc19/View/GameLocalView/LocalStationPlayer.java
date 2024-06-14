package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class contains the information that represent the station of a player that the view can access.
 * It is created by the {@link LocalModel}
 * The class is abstract and is extended by two other classes:
 * - {@link OtherStation}, that represents another player's station.
 * - {@link PersonalStation}, that represents the personal station.
 */
public abstract class LocalStationPlayer {

    /**
     * The player that owns the Station.
     */
    protected final String ownerPlayer;

    /**
     * The number of points of the station so far.
     */
    protected int numPoints;

    /**
     * The {@link PlayableCard} placed so far.
     */
    protected final PlayableCard[][] cardSchema;

    /**
     * The {@link Symbol} visible so far in the table.
     */
    protected Map<Symbol, Integer> visibleSymbols;

    /**
     * A list containing for each {@link PlayableCard} placed its position in the station schema.
     */
    protected List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    /**
     * The {@link Color} chosen by the player.
     */
    protected Color chosenColor;

    public LocalStationPlayer(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                              int numPoints, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];

        this.ownerPlayer = nicknameOwner;
        this.chosenColor = chosenColor;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.placedCardSequence = new ArrayList<>(placedCardSequence);

        reconstructSchema();
    }

    /**
     * The methods initialized the {@link #cardSchema} and creates it from the content of the {@link #placedCardSequence}.
     * First each element of the {@link #cardSchema} is {@code null}, then the position that are occupied according to the
     * {@link #placedCardSequence} are filled.
     */
    public void reconstructSchema() {
        // Reset card schema
        for (int i = 0; i < ImportantConstants.gridDimension; i++) {
            for (int k = 0; k < ImportantConstants.gridDimension; k++) {
                cardSchema[i][k] = null;
            }
        }

        // Reconstruct card schema and overlap from placedCardSequence
        for (Tuple<PlayableCard, Tuple<Integer,Integer>> placement : placedCardSequence) {
            PlayableCard card = placement.x();
            Tuple<Integer, Integer> position = placement.y();
            int rowIndex = position.x();
            int colIndex = position.y();

            // Update card schema with the placed card
            cardSchema[rowIndex][colIndex] = card;
        }
    }

    /**
     * To set the {@link Color} chosen by the players.
     * @param chosenColor, the color chosen by the player.
     */
    public void setChosenColor(Color chosenColor){
        this.chosenColor = chosenColor;
    }

    /**
     * To set the private goal card from one of the two private goal cards.
     * @param cardIdx the index of the card from the array of private goal cards.
     */
    public abstract void setPrivateGoalCard(int cardIdx);

    /**
     * To set the private goal card from one of the two private goal cards.
     * @param goalCard the chosen {@link GoalCard} from the array of private goal cards.
     */
    public abstract void setPrivateGoalCard(GoalCard goalCard);

    /**
     * To return
     * @return the nickname of the owner of the station.
     */
    public String getOwnerPlayer() {
        return ownerPlayer;
    }

    /**
     * To return
     * @return the number of points of the station.
     */
    public int getNumPoints() {
        return numPoints;
    }

    /**
     * To return
     * @return the visible symbols in the stations.
     */
    public  Map<Symbol, Integer> getVisibleSymbols() {
        return visibleSymbols;
    }

    /**
     * To return
     * @return the color chosen.
     */
    public Color getChosenColor() {
        return chosenColor;
    }

    /**
     * To set the number of points of the station.
     * @param numPoints the number of points of the station.
     */
    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }

    /**
     * To set the visible symbols of the station.
     * @param visibleSymbols a map containing for each {@link Symbol} the number of times it is visible.
     */
    public void setVisibleSymbols(Map<Symbol, Integer> visibleSymbols){
         this.visibleSymbols = new HashMap<>(visibleSymbols);
    }

    /**
     * Method used to place a card in the station given:
     * @param anchorCardCode the anchor card code of the card we want to place the card from.
     * @param cardToPlace the card we want to place.
     * @param direction the direction in which we want to place the card, given the anchor card.
     */
    public abstract void placeCard(PlayableCard cardToPlace, String anchorCardCode, Direction direction);

    /**
     * To place the initial card in the station.
     * @param initialCard the initial card the user wants to place in the station.
     */
    public void placeInitialCard(PlayableCard initialCard){
        this.placedCardSequence.add(new Tuple<>(initialCard, new Tuple<>((ImportantConstants.gridDimension / 2), (ImportantConstants.gridDimension / 2))));
        this.cardSchema[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = initialCard;
    }

    /**
     * The coordinates of the card whose {@code cardCode} is given.
     * @param cardCode the card code of the card.
     * @return the coordinates of the card if it was previously placed,
     *          {@code (-1, -1)} otherwise.
     */
    public Tuple<Integer, Integer> getCoords(String cardCode){
        for(Tuple<PlayableCard, Tuple<Integer,Integer>> t : placedCardSequence){
            if(t.x().getCardCode().equals(cardCode)){
                return t.y();
            }
        }
        return new Tuple<>(-1, -1);
    }

    /**
     * Returns the card at given position, else {@code null}
     * @param x first dimension of the matrix
     * @param y second dimension of the matrix
     * @return the card at given position, if the card is not present returns {@code null}
     */
    public PlayableCard getPlacedCardAtPosition(int x, int y) {
        return this.cardSchema[x][y];
    }

    /**
     * To return the list of placed card and their positions.
     * @return the list containing the card placed in the station so far and their position.
     */
    public List<Tuple<PlayableCard, Tuple<Integer,Integer>>> getPlacedCardSequence(){
        return this.placedCardSequence;
    }

    /**
     * To ask if a card is placeable given:
     * @param cardToPlace the card we want to place.
     * @param anchor the anchor card from which we want to place the card.
     * @param direction the direction in which we want to place the card.
     * @return a boolean that is true only if the card can be placed in that position.
     */
    public abstract boolean cardIsPlaceable(PlayableCard cardToPlace, PlayableCard anchor, Direction direction);

}