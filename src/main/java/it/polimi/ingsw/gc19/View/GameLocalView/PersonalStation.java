package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonalStation extends LocalStationPlayer {

    private final ArrayList<PlayableCard> cardsInHand;
    private GoalCard privateGoalCardInStation;
    private final GoalCard[] privateGoalCardsInStation;

    public PersonalStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols, int numPoints,
                           List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence,
                           GoalCard privateGoalCard, GoalCard privateGoalCardInStation1, GoalCard privateGoalCardInStation2,
                           List<PlayableCard> cardsInHand) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.cardsInHand = new ArrayList<>(cardsInHand);

        privateGoalCardsInStation = new GoalCard[]{privateGoalCardInStation1, privateGoalCardInStation2};
        setPrivateGoalCard(privateGoalCard);
    }

    public  void updateCardsInHand(PlayableCard cardToAdd){
        cardsInHand.add(cardToAdd);
    }

    @Override
    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) {
        // checks if the card to place is contained in the player's hand
        if(!this.getCardsInHand().contains(toPlace)){
            throw new RuntimeException();
        }


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

    public ArrayList<PlayableCard> getCardsInHand() {
        return cardsInHand;
    }

    public void setPrivateGoalCard(GoalCard goalCard) {
        if(goalCard == null) return; // obtained a new personal station but the card was not chosen.
        if (privateGoalCardsInStation[0] != null && privateGoalCardsInStation[1] != null) {
            // choosing the private goal card
            int cardIdx = goalCard.equals(privateGoalCardsInStation[0]) ? 0 : 1;
            setPrivateGoalCard(cardIdx);
        }
    }

    public void placeCard(PlayableCard cardToPlace, String anchorCardCode, Direction direction){
        Tuple<Integer, Integer> coord = getCoord(anchorCardCode);
        coord = new Tuple<>(direction.getX() + coord.x(), direction.getY() + coord.y());

        this.cardsInHand.remove(cardToPlace);

        placedCardSequence.add(new Tuple<>(cardToPlace, coord));
        cardSchema[coord.x()][coord.y()] = cardToPlace;

    }

    public  void setPrivateGoalCard(int cardIdx) {
        this.privateGoalCardInStation = privateGoalCardsInStation[cardIdx];
    }

    public GoalCard getPrivateGoalCardInStation() {
        return privateGoalCardInStation;
    }

    public GoalCard[] getPrivateGoalCardsInStation() {
        return privateGoalCardsInStation;
    }
}
