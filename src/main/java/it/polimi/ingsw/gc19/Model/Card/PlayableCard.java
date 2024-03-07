package it.polimi.ingsw.gc19.Model.Card;


import it.polimi.ingsw.gc19.Model.Enums.*;

import it.polimi.ingsw.gc19.Model.Station.Station;

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
    private CardState cardState = new CardUp();

    protected PlayableCard(String cardCode) {
        super(cardCode);
    }

    //Methods exposed by PlayableCard externally
    public PlayableCardType getCardType(){
        return this.cardType;
    }
    public CornerValue getCornerByDirection(Direction direction){
        return this.cardState.getCornerByDirection(direction);
    }

    public CornerValue getCornerByCoords(int x, int y){
        return this.cardState.getCornerByCoords(x, y);
    }

    public boolean enoughResourceToBePlaced(Station station){
        return this.cardState.enoughResourceToBePlaced(station);
    }

    public boolean canPlaceOver(Direction direction){
        return this.cardState.canPlaceOver(direction);
    }

    public int countPoints(Station station){
        return this.cardState.countPoints(station, this);
    }

    public ArrayList<Symbol> getPermanentResources(){
        return this.cardState.getPermanentResources();
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
        public CornerValue getCornerByDirection(Direction direction);
        public CornerValue getCornerByCoords(int x, int y);
        public boolean enoughResourceToBePlaced(Station station);
        public int countPoints(Station station, PlayableCard card);
        public boolean canPlaceOver(Direction direction);
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
        public CornerValue getCornerByDirection(Direction direction){
            return frontGridConfiguration[direction.getX()][direction.getY()];
        }

        @Override
        public CornerValue getCornerByCoords(int x, int y){
            return frontGridConfiguration[x][y];
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
        public boolean canPlaceOver(Direction direction){
            return frontGridConfiguration[direction.getX()][direction.getY()] == EmptyCorner.EMPTY;
        }

        @Override
        public int countPoints(Station station, PlayableCard card){
            return playableEffect.countPoints(station, card);
        }

        @Override
        public ArrayList<Symbol> getPermanentResources() {
            return new ArrayList<>();
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
        public CornerValue getCornerByDirection(Direction direction){
            return backGridConfiguration[direction.getX()][direction.getY()];
        }

        @Override
        public CornerValue getCornerByCoords(int x, int y){
            return backGridConfiguration[x][y];
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
        public int countPoints(Station station, PlayableCard card){
            return 0;
        }

        @Override
        public boolean canPlaceOver(Direction direction){
            return backGridConfiguration[direction.getX()][direction.getY()] == EmptyCorner.EMPTY;
        }

        @Override
        public ArrayList<Symbol> getPermanentResources() {
            return permanentResources;
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