package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class Station{

    private final ArrayList<Card> cardsInStation;
    private final HashMap<Symbol, Integer> visibleSymbolsInStation;
    private GoalCard privateGoalCard;
    private int numPoints;
    private final CardSchema cardSchema;

    public Station(){
        this.numPoints = 0;
        this.privateGoalCard = null;
        this.visibleSymbolsInStation = new HashMap<>();

        for(Symbol s : Symbol.getResources()){
            this.visibleSymbolsInStation.put(s, 0);
        }
        for(Symbol s : Symbol.getObjects()){
            this.visibleSymbolsInStation.put(s, 0);
        }

        this.cardsInStation = new ArrayList<>();

        this.cardSchema = new CardSchema();
    }

    public HashMap<Symbol, Integer> getVisibleSymbolsInStation(){
        return this.visibleSymbolsInStation;
    }

    //da capire meglio una volta introdotto il json
    private void setPrivateGoalCard(GoalCard privateGoalCard){
        this.privateGoalCard = privateGoalCard;
    }

    public void updatePoints(){

    }

    public CardSchema getCardSchema() {
        return this.cardSchema;
    }

    public boolean cardIsPlaceable(Card anchor, Direction direction){ /*Controlli su NullPointerException, ancora invalida, direction invalida da far fare al controllore*/
        return false;
    }

    public Card getCardWithAnchor(PlayableCard card, Direction d) {
        return null;
    }

    //questo metodo potrebbe essere un esempio di ciò che può fare il controllore
    /*public boolean cardInStation(Card card){
        return  this.cardsInStation.stream().anyMatch(card::equals) ||
                Arrays.stream(this.cardSchema).map(x -> Arrays.stream(x)).anyMatch(card::equals);
    }*/
}
