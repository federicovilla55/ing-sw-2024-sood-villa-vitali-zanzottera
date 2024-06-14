package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class contains the information of a personal station of a player.
 * It extends the class {@link LocalStationPlayer} and adds the following information:
 * - the list of cards in hand;
 * - the private goal card chosen;
 * - the list of private goal cards from which to choose the private goal card;
 * - the initial card.
 */
public class PersonalStation extends LocalStationPlayer {

    /**
     * The array of the PlayableCards that the client has in its hand.
     */
    private final ArrayList<PlayableCard> cardsInHand;

    /**
     * The private goal card that the user has chosen.
     */
    private GoalCard privateGoalCardInStation;

    /**
     * The array of cards from which the user can choose its private goal card.
     */
    private final GoalCard[] privateGoalCardsInStation;

    /**
     * The initial card that the user can place during the setup phase of the game.
     */
    private final PlayableCard initialCard;

    public PersonalStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols, int numPoints,
                           List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence,
                           GoalCard privateGoalCard, GoalCard privateGoalCardInStation1, GoalCard privateGoalCardInStation2,
                           List<PlayableCard> cardsInHand, PlayableCard initialcard) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.cardsInHand = new ArrayList<>(cardsInHand);
        this.initialCard = initialcard;

        privateGoalCardsInStation = new GoalCard[]{privateGoalCardInStation1, privateGoalCardInStation2};
        setPrivateGoalCard(privateGoalCard);
    }

    /**
     * To add a card in the player hand.
     * @param cardToAdd the {@link PlayableCard} that the user wants to add in the hand
     */
    public void updateCardsInHand(PlayableCard cardToAdd){
        cardsInHand.add(cardToAdd);
    }

    /**
     * To ask if a card is placeable in the {@link PersonalStation} of the client.
     * @param toPlace the card the client wants to place.
     * @param anchor the anchor from which the client wants to place the card.
     * @param direction the direction of the cardToPlace from the anchor.
     * @return a boolean containing {@code true} only if the card is placeable.
     */
    @Override
    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) {

        if(anchor == null || toPlace == null) return false;

        //the card in hand is always UP
        CardOrientation cardOrientation = toPlace.getCardOrientation();
        // checks if the card to place is contained in the player's hand
        if(!this.getCardsInHand().contains(toPlace.setCardState(CardOrientation.UP))){
            return false;
        }
        toPlace.setCardState(cardOrientation);

        if(!toPlace.enoughResourceToBePlaced(this.getVisibleSymbols())){
            return false;
        }

        int currentX = -1;
        int currentY = -1;

        boolean containedAnchor = false;
        for(Tuple<PlayableCard, Tuple<Integer,Integer>> t : placedCardSequence){
            if(t.x().equals(anchor)){
                Tuple<Integer, Integer> pos = t.y();
                currentX = pos.x() + direction.getX();
                currentY = pos.y() + direction.getY();
                containedAnchor = true;
                break;
            }
        }
        if(!containedAnchor || this.cardSchema[currentX][currentY] != null){
            return false;
        }

        for(Direction dir : Direction.values()){
                PlayableCard neighborCard = this.cardSchema[currentX + dir.getX()][currentY + dir.getY()];
            if(neighborCard != null && !neighborCard.canPlaceOver(dir.getOtherCornerPosition())){
                return false;
            }
        }
        return true;
    }

    /**
     * To return an ArrayList containing the {@link PlayableCard} in the player hand.
     * @return an ArrayList containing the {@link PlayableCard} in the player hand.
     */
    public ArrayList<PlayableCard> getCardsInHand() {
        return cardsInHand;
    }

    /**
     *
     * @param goalCard the chosen GoalCard from the array of private goal cards.
     */
    public void setPrivateGoalCard(GoalCard goalCard) {
        if(goalCard == null) return; // obtained a new personal station but the card was not chosen.
        if (privateGoalCardsInStation[0] != null && privateGoalCardsInStation[1] != null) {
            // choosing the private goal card
            int cardIdx = goalCard.equals(privateGoalCardsInStation[0]) ? 0 : 1;
            setPrivateGoalCard(cardIdx);
        }
    }

    /**
     * Method used to place a card in the {@link PersonalStation} given:
     * @param anchorCardCode the anchor card code of the card we want to place the card from.
     * @param cardToPlace the card we want to place.
     * @param direction the direction in which we want to place the card, given the anchor card.
     */
    public void placeCard(PlayableCard cardToPlace, String anchorCardCode, Direction direction){
        Tuple<Integer, Integer> coord = getCoords(anchorCardCode);
        coord = new Tuple<>(direction.getX() + coord.x(), direction.getY() + coord.y());

        /*
        the card in hand is always UP, and we need to make sure that when removing the card received
        from server is also UP to remove the card from the hand
        */
        CardOrientation cardOrientation = cardToPlace.getCardOrientation();
        this.cardsInHand.remove(cardToPlace.setCardState(CardOrientation.UP));
        cardToPlace.setCardState(cardOrientation);

        placedCardSequence.add(new Tuple<>(cardToPlace, coord));
        cardSchema[coord.x()][coord.y()] = cardToPlace;
    }

    /**
     * To return the initial card of the {@link PersonalStation}.
     * @return the initial card of the {@link PersonalStation}.
     */
    public PlayableCard getInitialCard() {
        return initialCard;
    }

    /**
     * To set the private goal card from the array of playable cards.
     * @param cardIdx the index of the card from the array of private goal cards.
     */
    public  void setPrivateGoalCard(int cardIdx) {
        this.privateGoalCardInStation = privateGoalCardsInStation[cardIdx];
    }

    /**
     * To obtain the private goal card chosen.
     * @return the private goal card chosen by the client.
     */
    public GoalCard getPrivateGoalCardInStation() {
        return privateGoalCardInStation;
    }

    /**
     * To obtain the array of possible goal cards from which
     * the client can choose its private goal card.
     * @return the array of possible goal cards from which
     *          the client can choose its private goal card
     */
    public GoalCard[] getPrivateGoalCardsInStation() {
        return privateGoalCardsInStation;
    }
}
