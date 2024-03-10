package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class Station{

    private final ArrayList<PlayableCard> cardsInStation;
    private final HashMap<Symbol, Integer> visibleSymbolsInStation;
    private GoalCard privateGoalCard;
    private int numPoints;
    private final CardSchema cardSchema;

    public Station(){
        this.numPoints = 0;
        this.privateGoalCard = null;
        this.visibleSymbolsInStation = new HashMap<>();

        for(Symbol s : Symbol.values()){
            this.visibleSymbolsInStation.put(s, 0);
        }

        this.cardsInStation = new ArrayList<>();

        this.cardSchema = new CardSchema();
    }

    public HashMap<Symbol, Integer> getVisibleSymbolsInStation(){
        return this.visibleSymbolsInStation;
    }

    public void updateCardsInHand(PlayableCard toInsert){
        this.cardsInStation.add(toInsert);
    }

    //da capire meglio una volta introdotto il json
    private void setPrivateGoalCard(GoalCard privateGoalCard){
        this.privateGoalCard = privateGoalCard;
    }

    public void updatePoints(Card card){
        numPoints = numPoints + card.countPoints(this);
    }

    public CardSchema getCardSchema() {
        return this.cardSchema;
    }

    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidPositionException, InvalidAnchorException{
        /*Controlli su NullPointerException, ancora invalida, direction invalida da far fare al controllore*/
        if(!this.cardsInStation.contains(toPlace)){
            throw new InvalidCardException();
        }
        return this.cardSchema.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this) && anchor.canPlaceOver(direction.getCornerInDirection());
    }

    public void placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidPositionException, InvalidAnchorException{
        PlayableCard sharingCorner;
        if(this.cardIsPlaceable(anchor, toPlace, direction)){
            placeCard(anchor, toPlace, direction);
            updatePoints(toPlace);
            for(Symbol s : Symbol.values()){
                this.visibleSymbolsInStation.put(s, this.visibleSymbolsInStation.get(s) + toPlace.getHashMapSymbols().get(s));
            }
            for(Direction d : Direction.values()){
                try{
                    sharingCorner = getCardSchema().getCardWithAnchor(toPlace, d);
                    if(toPlace.getCorner(d.getCornerInDirection()) != EmptyCorner.EMPTY) this.visibleSymbolsInStation.compute((Symbol) anchor.getCorner(d.getCornerInDirection()), (k, v) -> v - 1);
                }
                catch(Exception ignored){};
            }
        }
    }

    public ArrayList<PlayableCard> getCardsInHand(){
        return this.cardsInStation;
    }

}
