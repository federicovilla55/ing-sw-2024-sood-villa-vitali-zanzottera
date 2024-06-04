package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class represents a station of another player, whose nickname is different from the client.
 * The class extends {@link LocalStationPlayer}.
 * It is created by the {@link LocalModel} class.
 */
public class OtherStation extends LocalStationPlayer{
    /**
     * Object uses as a Lock for the changes in the backCardHand list.
     */
    private final Object backHandLock;

    /**
     * The object represent another player hand. Because the client cannot have access
     * to another player's hand, it can just show the back of the cards, so the
     * Tuple of {@link Symbol} and {@link PlayableCardType}
     */
    List<Tuple<Symbol,PlayableCardType>> backCardHand;

    public OtherStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                        int numPoints, List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.backHandLock = new Object();
        this.backCardHand = new ArrayList<>();
    }

    public OtherStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                        int numPoints, List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence,
                        List<Tuple<Symbol,PlayableCardType>> backCardHand) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.backHandLock = new Object();
        this.backCardHand = new ArrayList<>(backCardHand);
    }

    /**
     * To return
     * @return the Tuple of {@link Symbol} and {@link PlayableCardType} that the player who own the station has in its hand.
     */
    public List<Tuple<Symbol,PlayableCardType>> getBackCardHand() {
        synchronized (backHandLock) {
            return backCardHand;
        }
    }

    /**
     * To add a Tuple of {@link Symbol} and {@link PlayableCardType}
     * @param cardToAdd, the Tuple of {@link Symbol} and {@link PlayableCardType} to add
     *                   to the cards in hand.
     */
    public void addBackCard(Tuple<Symbol,PlayableCardType> cardToAdd){
        synchronized (backHandLock){
            backCardHand.add(cardToAdd);
        }
    }

    /**
     * To place a card in the station given:
     * @param cardToPlace, the {@link PlayableCard} to place.
     * @param anchorCardCode, the card code of the anchor from which to place the card.
     * @param direction, the direction in which the player want to place its card.
     */
    public void placeCard(PlayableCard cardToPlace, String anchorCardCode, Direction direction){
        Tuple<Integer, Integer> coord = getCoord(anchorCardCode);
        coord = new Tuple<>(direction.getX() + coord.x(), direction.getY() + coord.y());

        Tuple<Symbol,PlayableCardType> cardToRemove = new Tuple<>(cardToPlace.getSeed(),cardToPlace.getCardType());

        this.backCardHand.remove(cardToRemove);

        placedCardSequence.add(new Tuple<>(cardToPlace, coord));
        cardSchema[coord.x()][coord.y()] = cardToPlace;

    }

    /**
     * To ask if a card is placeable given. For other stations it returns always false.
     * @param cardToPlace, the card we want to place.
     * @param anchor, the anchor card from which we want to place the card.
     * @param direction, the direction in which we want to place the card.
     * @return a boolean that is true only if the card can be placed in that position.
     */
    @Override
    public boolean cardIsPlaceable(PlayableCard cardToPlace, PlayableCard anchor, Direction direction) {
        return false;
    }

    /**
     * Returns a UnsupportedOperationException because the Client cannot have access to which card
     * another player has chosen as their private Goal card.
     * @param goalCard the wrongly chosen GoalCard from the array of private goal cards.
     */
    @Override
    public void setPrivateGoalCard(GoalCard goalCard) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a UnsupportedOperationException because the Client cannot have access to which card
     * another player has chosen as their private Goal card.
     * @param cardIdx the wrongly chosen index of the card from the array of private goal cards.
     */
    @Override
    public void setPrivateGoalCard(int cardIdx) {
        throw new UnsupportedOperationException();
    }

    /**
     * To set the cards that another player has in its hand.
     * @param backCardHand, the list of Tuple of {@link Symbol} and {@link PlayableCardType}
     *                      containing the hand of another player.
     */
    public void setBackCardHand(ArrayList<Tuple<Symbol,PlayableCardType>> backCardHand) {
        this.backCardHand = new ArrayList<>(backCardHand);
    }

}
