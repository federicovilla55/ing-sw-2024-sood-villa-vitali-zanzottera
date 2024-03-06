package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Station.Station;

import java.util.*;

public class PlayableCard extends Card{

    //Type
    private PlayableCardType cardType;

    //Front card attributes
    private CornerValue[][] frontGridConfiguration;
    private PlayableEffect playableEffect;
    private HashMap<Symbol, Integer> requiredSymbolToPlace;

    //Back card attributes
    private CornerValue[][] backGridConfiguration;
    private ArrayList<Symbol> permanentResources;

    //Card state
    private CardState cardState;

    protected PlayableCard(String cardCode) {
        super(cardCode);
    }

    public void setCardState(CardState cardState){
        this.cardState = cardState;
    }

    //Methods exposed by PlayableCard externally
    public PlayableCardType getCardType(){
        return this.cardType;
    }
    public CornerValue getUpLeft(){
        return this.cardState.getUpLeft();
    }
    public CornerValue getUpRight(){
        return this.cardState.getUpRight();
    }

    public CornerValue getDownLeft(){
        return this.cardState.getDownLeft();
    }

    public CornerValue getDownRight() {
        return this.cardState.getDownRight();
    }

    public boolean isPlaceable(Station station){
        return this.cardState.isPlaceable(station);
    }

    public int countPoints(Station station){
        return this.cardState.countPoints(station, this);
    }

    public void swapCard(){
        this.cardState.swap();
    }

    @Override
    public String getCardDescription(){
        return "Type: " + cardType +
                "Front grid configuration: DOWN-LEFT = " + frontGridConfiguration[0][0] + " UP-LEFT = " + frontGridConfiguration[1][0] + " UP-RIGHT = " + frontGridConfiguration[1][1] + " DOWN-RIGHT = " + frontGridConfiguration[0][1] +
                "Front face effect: " + this.playableEffect.getEffectDescription() +
                "Required symbols to place front: " + this.requiredSymbolToPlace.toString() +
                "Back grid configuration: DOWN-LEFT = " + backGridConfiguration[0][0] + " UP-LEFT = " + backGridConfiguration[1][0] + " UP-RIGHT = " + backGridConfiguration[1][1] + " DOWN-RIGHT = " + backGridConfiguration[0][1] +
                "Permanent resources: " + permanentResources.toString();
    }

    public CardOrientation getCardOrientation(){
        return cardState.getState();
    }

    interface CardState{

        public void swap();
        public CornerValue getUpLeft();
        public CornerValue getUpRight();
        public CornerValue getDownLeft();
        public CornerValue getDownRight();
        public boolean isPlaceable(Station station);
        public int countPoints(Station station, PlayableCard card);
        public CardOrientation getState();
        public HashMap<Symbol, Integer> getHashMapSymbols();

    }

    class CardUp implements CardState{
        @Override
        public void swap(){
            cardState = new CardDown();
        }

        @Override
        public CornerValue getUpLeft(){
            return frontGridConfiguration[1][0];
        }

        @Override
        public CornerValue getUpRight(){
            return frontGridConfiguration[1][1];
        }

        @Override
        public CornerValue getDownLeft(){
            return frontGridConfiguration[0][0];
        }

        @Override
        public CornerValue getDownRight(){
            return frontGridConfiguration[0][1];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.UP;
        }

        @Override
        public boolean isPlaceable(Station station){
            for(Symbol s : Symbol.values()){
                if(station.getVisibleSymbolsInStation().get(s) < requiredSymbolToPlace.get(s)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public int countPoints(Station station, PlayableCard card){
            return playableEffect.countPoints(station, card);
        }

        public HashMap<Symbol, Integer> getHashMapSymbols(){
            HashMap<Symbol, Integer> symbolHashMap = new HashMap<>();

            for(Symbol s : Symbol.getObjects()){
                symbolHashMap.put(s, 0);
            }

            for(Symbol s : Symbol.getResources()){
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

    class CardDown implements CardState{
        @Override
        public void swap(){
            cardState = new CardUp();
        }

        @Override
        public CornerValue getUpLeft(){
            return backGridConfiguration[1][0];
        }

        @Override
        public CornerValue getUpRight(){
            return backGridConfiguration[1][1];
        }

        @Override
        public CornerValue getDownLeft(){
            return backGridConfiguration[0][0];
        }

        @Override
        public CornerValue getDownRight(){
            return backGridConfiguration[0][1];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.DOWN;
        }

        @Override
        public boolean isPlaceable(Station station){
            return true;
        }

        @Override
        public int countPoints(Station station, PlayableCard card){
            return 0;
        }

        @Override
        public HashMap<Symbol, Integer> getHashMapSymbols(){

            HashMap<Symbol, Integer> symbolHashMap = new HashMap<>();

            for(Symbol s : Symbol.getObjects()){
                symbolHashMap.put(s, 0);
            }

            for(Symbol s : Symbol.getResources()){
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