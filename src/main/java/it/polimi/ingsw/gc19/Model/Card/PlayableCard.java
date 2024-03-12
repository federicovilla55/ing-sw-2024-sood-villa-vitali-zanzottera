package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.*;
import it.polimi.ingsw.gc19.Model.Enums.*;

import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.*;

public class PlayableCard extends Card{

    private PlayableCardType cardType;
    private CornerValue[][] frontGridConfiguration;
    private HashMap<Symbol, Integer> requiredSymbolToPlace;
    private CornerValue[][] backGridConfiguration;
    private ArrayList<Symbol> permanentResources;
    private CardState cardState;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "effect_type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NoConstraintEffect.class, name = "no_constraint"),
            @JsonSubTypes.Type(value = CornerEffect.class, name = "corner"),
            @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
    })
    private PlayableEffect playableEffect;

    @JsonCreator
    public PlayableCard(
            @JsonProperty("code") String cardCode,
            @JsonProperty("card_type") PlayableCardType cardType,
            @JsonProperty("front_grid") CornerValue[][] frontGridConfiguration,
            @JsonProperty("required_symbol") HashMap<Symbol, Integer> requiredSymbolToPlace,
            @JsonProperty("back_grid") CornerValue[][] backGridConfiguration,
            @JsonProperty("permanent") ArrayList<Symbol> permanentResources,
            @JsonProperty("effect_type") PlayableEffect playableEffect
    ){
        super(cardCode);
        this.cardType = cardType;
        this.frontGridConfiguration = frontGridConfiguration;
        this.requiredSymbolToPlace = requiredSymbolToPlace;
        this.backGridConfiguration = backGridConfiguration;
        this.permanentResources = permanentResources;
        this.playableEffect = playableEffect;
        this.cardState = new CardDown();
    }

    protected PlayableCard(String cardCode) {
        super(cardCode);
    }

    public PlayableCardType getCardType(){
        return this.cardType;
    }

    public CornerValue getCorner(CornerPosition position){
        return this.cardState.getCorner(position);
    }

    public boolean enoughResourceToBePlaced(Station station){
        return this.cardState.enoughResourceToBePlaced(station);
    }

    public boolean canPlaceOver(CornerPosition position){
        return this.cardState.canPlaceOver(position);
    }

    public int countPoints(Station station){
        return this.cardState.countPoints(station);
    }

    public ArrayList<Symbol> getPermanentResources(){
        return this.cardState.getPermanentResources();
    }

    public HashMap<Symbol, Integer> getHashMapSymbols(){
        return this.cardState.getHashMapSymbols();
    }

    public void swapCard(){
        this.cardState.swap();
    }

    @Override
    public String getCardDescription(){
        return "Type: " + cardType +
                "Front grid configuration: DOWN-LEFT = " + frontGridConfiguration[0][0] + " UP-LEFT = " + frontGridConfiguration[1][0] + " UP-RIGHT = " + frontGridConfiguration[1][1] + " DOWN-RIGHT = " + frontGridConfiguration[0][1] +
                this.playableEffect.getEffectDescription() +
                "Required symbols to place front: " + this.requiredSymbolToPlace.toString() +
                "Back grid configuration: DOWN-LEFT = " + backGridConfiguration[0][0] + " UP-LEFT = " + backGridConfiguration[1][0] + " UP-RIGHT = " + backGridConfiguration[1][1] + " DOWN-RIGHT = " + backGridConfiguration[0][1] +
                "Permanent resources: " + permanentResources.toString();
    }

    public CardOrientation getCardOrientation(){
        return cardState.getState();
    }

    private interface CardState{

        void swap();
        CornerValue getCorner(CornerPosition position);
        boolean enoughResourceToBePlaced(Station station);
        int countPoints(Station station);
        public boolean canPlaceOver(CornerPosition position);
        public CardOrientation getState();
        public ArrayList<Symbol> getPermanentResources();
        public HashMap<Symbol, Integer> getHashMapSymbols();

    }

    private class CardUp implements CardState{
        @Override
        public void swap(){
            cardState = new CardDown();
        }

        @Override
        public CornerValue getCorner(CornerPosition position){
            return frontGridConfiguration[position.getX()][position.getY()];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.UP;
        }

        @Override
        public boolean enoughResourceToBePlaced(Station station){
            for(Symbol s : Symbol.values()){
                if(station.getVisibleSymbolsInStation().get(s) < requiredSymbolToPlace.get(s)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canPlaceOver(CornerPosition position){
            return frontGridConfiguration[position.getX()][position.getY()] != NotAvailableCorner.NOT_AVAILABLE;
        }

        @Override
        public int countPoints(Station station){
            return playableEffect.countPoints(station);
        }

        @Override
        public ArrayList<Symbol> getPermanentResources() {
            return new ArrayList<>();
        }

        @Override
        public HashMap<Symbol, Integer> getHashMapSymbols(){
            HashMap<Symbol, Integer> symbolHashMap = new HashMap<>();
            for(Symbol s : Symbol.values()){
                symbolHashMap.put(s, 0);
            }
            for(CornerValue[] row : frontGridConfiguration){
                for(CornerValue c : row){
                    if(c.hasSymbol()){
                        symbolHashMap.compute((Symbol) c, (k, v) -> v + 1);
                    }
                }
            }
            return symbolHashMap;
        }

    }

    private class CardDown implements CardState{
        @Override
        public void swap(){
            cardState = new CardUp();
        }

        @Override
        public CornerValue getCorner(CornerPosition position){
            return backGridConfiguration[position.getX()][position.getY()];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.DOWN;
        }

        @Override
        public boolean enoughResourceToBePlaced(Station station){
            return true;
        }

        @Override
        public int countPoints(Station station){
            return 0;
        }

        @Override
        public boolean canPlaceOver(CornerPosition position){
            return backGridConfiguration[position.getX()][position.getY()] != NotAvailableCorner.NOT_AVAILABLE;
        }

        @Override
        public ArrayList<Symbol> getPermanentResources() {
            return permanentResources;
        }

        @Override
        public HashMap<Symbol, Integer> getHashMapSymbols(){
            HashMap<Symbol, Integer> symbolHashMap = new HashMap<>();
            for(Symbol s : Symbol.values()){
                symbolHashMap.put(s, 0);
            }
            for(Symbol s : permanentResources){
                symbolHashMap.compute(s, (k, v) -> v + 1);
            }
            for(CornerValue[] row : backGridConfiguration){
                for(CornerValue c : row){
                    if(c.hasSymbol()){
                        symbolHashMap.compute((Symbol) c, (k, v) -> v + 1);
                    }
                }
            }
            return symbolHashMap;
        }

    }

}