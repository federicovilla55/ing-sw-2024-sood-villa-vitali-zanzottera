package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public int getNumPoints(){
        return this.numPoints;
    }

    public GoalCard getPrivateGoalCard(){
        return this.privateGoalCard;
    }

    public ArrayList<PlayableCard> getCardsInStation(){
        return this.cardsInStation;
    }

    private void setPrivateGoalCard(GoalCard privateGoalCard){
        this.privateGoalCard = privateGoalCard;
    }

    public void updatePoints(Card card){
        numPoints = numPoints + card.countPoints(this);
    }

    public CardSchema getCardSchema() {
        return this.cardSchema;
    }

    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{
        if(!this.cardsInStation.contains(toPlace)){
            throw new InvalidCardException();
        }
        return this.cardSchema.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this) && anchor.canPlaceOver(direction.getThisCornerPosition());
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
                    getCardSchema().getCardWithAnchor(toPlace, d)
                                   .flatMap(x -> x.getCorner(d.getOtherCornerPosition()).getSymbol())
                                   .ifPresent(s -> this.visibleSymbolsInStation.compute(s, (k, v) -> v - 1));
                }
                catch(Exception ignored){};
            }
        }
    }

    public ArrayList<PlayableCard> getCardsInHand(){
        return this.cardsInStation;
    }

}
