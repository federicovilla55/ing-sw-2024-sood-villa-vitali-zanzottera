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

public class OtherStation extends LocalStationPlayer{
    private final Object backHandLock;
    List<PlayableCardType> backCardHand;

    public OtherStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                        int numPoints, List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.backHandLock = new Object();
        this.backCardHand = new ArrayList<>();
    }

    public OtherStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                        int numPoints, List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence,
                        List<PlayableCardType> backCardHand) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);

        this.backHandLock = new Object();
        this.backCardHand = new ArrayList<>(backCardHand);
    }

    public List<PlayableCardType> getBackCardHand() {
        synchronized (backHandLock) {
            return backCardHand;
        }
    }

    public void addBackCard(PlayableCardType cardToAdd){
        synchronized (backHandLock){
            backCardHand.add(cardToAdd);
        }
    }

    public void placeCard(PlayableCard cardToPlace, String anchorCardCode, Direction direction){
        Tuple<Integer, Integer> coord = getCoord(anchorCardCode);
        coord = new Tuple<>(direction.getX() + coord.x(), direction.getY() + coord.y());

        this.backCardHand.remove(cardToPlace.getCardType());

        placedCardSequence.add(new Tuple<>(cardToPlace, coord));
        cardSchema[coord.x()][coord.y()] = cardToPlace;

    }

    @Override
    public void setPrivateGoalCard(GoalCard goalCard) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrivateGoalCard(int cardIdx) {
        throw new UnsupportedOperationException();
    }

    public void setBackCardHand(ArrayList<PlayableCardType> backCardHand) {
        this.backCardHand = new ArrayList<>(backCardHand);
    }

}
