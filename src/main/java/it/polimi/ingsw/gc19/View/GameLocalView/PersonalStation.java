package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonalStation extends LocalStationPlayer {
    private final ArrayList<PlayableCard> cardsInHand;
    private Integer privateGoalCardIdx;
    private GoalCard privateGoalCardInStation;

    private GoalCard[] privateGoalCardsInStation;

    public PersonalStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols, int numPoints,
                           List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence,
                           GoalCard privateGoalCard, GoalCard privateGoalCardInStation1, GoalCard privateGoalCardInStation2) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);
        cardsInHand = new ArrayList<>();

        privateGoalCardsInStation = new GoalCard[]{privateGoalCardInStation1, privateGoalCardInStation2};
        setPrivateGoalCard(privateGoalCard);
    }

    public void updateCardsInHand(PlayableCard cardToAdd){
        cardsInHand.add(cardToAdd);
    }

    @Override
    public void placeCard(PlayableCard placedCard, Tuple<Integer, Integer> position) {
        cardsInHand.remove(placedCard);
        placedCardSequence.add(new Tuple<>(placedCard, position));

        cardSchema[position.x()][position.y()] = placedCard;

        cardPosition.put(placedCard, new Tuple<>(position.x(), position.y()));

        cardOverlap[position.x()][position.y()] = currentCount;
        currentCount++;
    }

    public void setColor(Color color){
        this.chosenColor = color;
    }

    public ArrayList<PlayableCard> getCardsInHand() {
        return cardsInHand;
    }

    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) {
        if(!this.getCardsInHand().contains(toPlace)){
            throw new RuntimeException();
        }
        if(!toPlace.enoughResourceToBePlaced(this.getVisibleSymbols())){
            return false;
        }
        if(!this.isPlaceable(anchor, direction)){
            return false;
        }
        return this.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this.getVisibleSymbols());
    }

    boolean isPlaceable(PlayableCard anchor, Direction direction) {
        PlayableCard neighborCard;
        int currentX, currentY;
        if(!this.cardPosition.containsKey(anchor)){
            throw new RuntimeException();
        }
        currentX = this.cardPosition.get(anchor).x() + direction.getX();
        currentY = this.cardPosition.get(anchor).y() + direction.getY();
        if(this.cardSchema[currentX][currentY] != null){
            return false;
        }

        for(Direction dir : Direction.values()){
            neighborCard = this.cardSchema[currentX + dir.getX()][currentY + dir.getY()];
            if(neighborCard != null && !neighborCard.canPlaceOver(dir.getOtherCornerPosition())){
                return false;
            }
        }
        return true;
    }

    public void setPrivateGoalCard(GoalCard goalCard) {
        if(goalCard == null) return; // obtained a new personal station but the card was not chosen.
        if(privateGoalCardsInStation[0] == null || privateGoalCardsInStation[1] == null){
            // obtained a personal station and the private goal was already chosen
            setPrivateGoalCard(goalCard);
        }else {
            // choosing the private goal card
            int cardIdx = goalCard.equals(privateGoalCardsInStation[0]) ? 0 : 1;
            setPrivateGoalCard(cardIdx);
        }
    }

    public void setPrivateGoalCard(int cardIdx) {
        this.privateGoalCardInStation = privateGoalCardsInStation[cardIdx];
    }

    public GoalCard getPrivateGoalCardInStation() {
        return privateGoalCardInStation;
    }
}
