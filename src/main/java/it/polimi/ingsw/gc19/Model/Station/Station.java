package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

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

    public void setPrivateGoalCard(GoalCard privateGoalCard){
        this.privateGoalCard = privateGoalCard;
    }

    public void updatePoints(Card card){ //METTERE UN METODO CHE SI OCCUPA SOLO DI AUMENTRE I PUNTEGGI RELATIVI AGLI OBBIETTIVI PRIVATE?
        this.numPoints = this.numPoints + card.countPoints(this);
    }

    public CardSchema getCardSchema() {
        return this.cardSchema;
    }

    public void placeInitialCard(PlayableCard initialCard){
        if(initialCard.getCardType() == PlayableCardType.INITIAL){
            initialCard.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
            this.cardSchema.placeInitialCard(initialCard);
        }
    }

    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{
        if(!this.cardsInStation.contains(toPlace)){
            throw new InvalidCardException();
        }
        return this.cardSchema.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this.visibleSymbolsInStation) && anchor.canPlaceOver(direction.getThisCornerPosition());
    }

    public boolean placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{ //ritornare un bool per dire se il piazzamento è andato a buon fine
        if(this.cardIsPlaceable(anchor, toPlace, direction)){
            this.cardSchema.placeCard(anchor, toPlace, direction);
            updatePoints(toPlace);
            toPlace.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
            for(Direction d : Direction.values()){
                try{
                    getCardSchema().getCardWithAnchor(toPlace, d)
                                   .flatMap(x -> x.getCorner(d.getOtherCornerPosition()).getSymbol())
                                   .ifPresent(s -> this.visibleSymbolsInStation.compute(s, (k, v) -> v - 1));
                }
                catch(Exception ignored){};
            }
            return true;
        }
        return false;
    }

    public ArrayList<PlayableCard> getCardsInHand(){
        return this.cardsInStation;
    }

}
