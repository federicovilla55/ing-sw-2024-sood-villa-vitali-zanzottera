package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.*;
import it.polimi.ingsw.gc19.Model.Enums.*;

import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.*;

/**
 * This class represents a single playable card
 */
@JsonTypeName("playable")
public class PlayableCard extends Card{

    /**
     * This attribute represents card type: Initial, Resource or Gold
     */
    private PlayableCardType cardType;

    /**
     * This attribute represents a 2x2 matrix used to store
     * information about the four front corners of the card.
     * A corner can be NOT_AVAILABLE, EMPTY or can contain a
     * symbol: ANIMAL, VEGETABLE, INSECT, MUSHROOM, INK,
     * FEATHER, SCROLL
     */

    private Corner[][] frontGridConfiguration;

    /** Symbols and quantity that a player needs to have to
     * place the card in the UP state in his station.
     */
    private HashMap<Symbol, Integer> requiredSymbolToPlace;

    /**
     * This attribute represents a 2x2 matrix used to store
     * information about the four back corners of the card.
     * A corner can be NOT_AVAILABLE, EMPTY or can contain a
     * symbol: ANIMAL, VEGETABLE, INSECT, MUSHROOM, INK,
     * FEATHER, SCROLL
     */
    private Corner[][] backGridConfiguration;
    /**
     * This attribute represents permanent resources
     * positioned at the center of the back of the card.
     * This resource can not be covered.
     */
    private ArrayList<Symbol> permanentResources;

    /**
     * This attribute represents the state of a card,
     * if it is on the front or on the back.
     */
    private CardState cardState;

    /**
     * This attribute represents the effect that a playable
     * card has, that is activated when the card is
     * positioned in a station in the UP state.
     * To see various effects, see classes that
     * implements PlayableEffect
     */
    private PlayableEffect playableEffect;
    @JsonCreator
    PlayableCard(
            @JsonProperty("code") String cardCode,
            @JsonProperty("playable_card_type") PlayableCardType cardType,
            @JsonProperty("front_grid") Corner[][] frontGridConfiguration,
            @JsonProperty("required_symbol") HashMap<Symbol, Integer> requiredSymbolToPlace,
            @JsonProperty("back_grid") Corner[][] backGridConfiguration,
            @JsonProperty("permanent") ArrayList<Symbol> permanentResources,
            @JsonProperty("effect_type") PlayableEffect playableEffect,
            @JsonProperty("orientation") CardState cardOrientation){

        super(cardCode);
        this.cardType = cardType;
        this.frontGridConfiguration = frontGridConfiguration;
        this.requiredSymbolToPlace = requiredSymbolToPlace;
        this.backGridConfiguration = backGridConfiguration;
        this.permanentResources = permanentResources;
        this.playableEffect = playableEffect;
        if (cardOrientation==null) this.cardState = new CardUp();

    }

    /**
     * This constructor creates a playable card
     * @param cardCode the code that uniquely identifies a card in a game
     */
    protected PlayableCard(String cardCode) {
        super(cardCode);
    }

    //Methods exposed by PlayableCard externally

    /**
     * This method returns the type of the card
     * @return this.cardType
     */
    public PlayableCardType getCardType(){
        return this.cardType;
    }

    /**
     * This method returns a specified card corner given a position
     * @param position a position among UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT
     * @return CornerValue type of specified corner
     */
    public Corner getCorner(CornerPosition position){
        return this.cardState.getCorner(position);
    }


    public boolean enoughResourceToBePlaced(HashMap<Symbol, Integer> freeResources){
        return this.cardState.enoughResourceToBePlaced(freeResources);
    }

    /**
     * This method returns true if and only if the specified corner is valid to
     * place a card on top of it
     * @param position a position among UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT
     * @return true if and only if the corner of chosen position is different
     * from NOT_AVAILABLE
     */
    public boolean canPlaceOver(CornerPosition position){
        return this.cardState.canPlaceOver(position);
    }

    /**
     * This method returns the points gained by the card effect
     * after its placement in a station. If the card is on the back,
     * this method returns 0
     * @param station the station where the card is placed
     * @return the points the card gives based on its effect
     * when placed
     */
    public int countPoints(Station station){
        return this.cardState.countPoints(station);
    }

    /**
     * This method returns permanent resources on the visible side
     * of the card depending on its state
     * @return ArrayList of Symbol of resources in the center
     * of the card (it is always empty when the card is on front)
     */
    public ArrayList<Symbol> getPermanentResources(){
        return this.cardState.getPermanentResources();
    }

    /**
     * This method computes symbols on the visible side of the card
     * depending on its state
     * @return HashMap of key Symbol and value Integer of symbols and their
     * quantities on the visible side of the card
     */
    public HashMap<Symbol, Integer> getHashMapSymbols(){
        return this.cardState.getHashMapSymbols();
    }

    /**
     * This method flips the card, switching between UP and DOWN
     * states
     */
    public PlayableCard swapCard(){
        this.cardState.swap();
        return this;
    }

    @Override
    public String getCardDescription(){
        return "Type: " + cardType +
                "Front grid configuration: DOWN-LEFT = " + frontGridConfiguration[1][0] + " UP-LEFT = " + frontGridConfiguration[0][0] + " UP-RIGHT = " + frontGridConfiguration[0][1] + " DOWN-RIGHT = " + frontGridConfiguration[1][1] +
                this.playableEffect.getEffectDescription() +
                "Required symbols to place front: " + this.requiredSymbolToPlace.toString() +
                "Back grid configuration: DOWN-LEFT = " + backGridConfiguration[1][0] + " UP-LEFT = " + backGridConfiguration[0][0] + " UP-RIGHT = " + backGridConfiguration[0][1] + " DOWN-RIGHT = " + backGridConfiguration[1][1] +
                "Permanent resources: " + permanentResources.toString();
    }

    /**
     * This method returns CardOrientation.UP or CardOrientation.DOWN
     * if the card is either in a state or the other
     * @return CardOrientation.UP if cardState is dynamically CardUp,
     * or CardOrientation.DOWN if cardState is dynamically CardDown
     */
    public CardOrientation getCardOrientation(){
        return cardState.getState();
    }

    public Symbol getSeed(){
        if(this.cardType != PlayableCardType.INITIAL){
            return this.permanentResources.getFirst();
        }
        return null;
    }

    /**
     * This interface is the state of a card, either up or down
     */
    private interface CardState{

        /**
         * This method switches the card state, if up to down,
         * if down to up
         */
        void swap();

        /**
         * This method returns a specified card corner given a position
         * @param position a position among UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT
         * @return CornerValue type of specified corner
         */
        Corner getCorner(CornerPosition position);

        boolean enoughResourceToBePlaced(HashMap<Symbol, Integer> freeResources);

        /**
         * This method returns the points gained by the card effect
         * after its placement in a station. If the card is on the back,
         * this method returns 0
         * @param station the station where the card is placed
         * @return the points the card gives based on its effect
         * when placed
         */
        int countPoints(Station station);        /**
         * This method returns true if and only if the specified corner is valid to
         * place a card on top of it
         * @param position a position among UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT
         * @return true if and only if the corner of chosen position is different
         * from NOT_AVAILABLE
         */
        boolean canPlaceOver(CornerPosition position);
        /**
         * This method returns CardOrientation.UP or CardOrientation.DOWN
         * if the card is either in a state or the other
         * @return CardOrientation.UP if cardState is dynamically CardUp,
         * or CardOrientation.DOWN id cardState is dynamically CardDown
         */
        CardOrientation getState();
        /**
         * This method returns permanent resources on the visible side
         * of the card depending on its state
         * @return ArrayList<Symbol> of resources in the center
         * of the card (it is always empty when the card is on front)
         */
        ArrayList<Symbol> getPermanentResources();
        /**
         * This method computes symbols on the visible side of the card
         * depending on its state
         * @return HashMap\<Symbol, Integer\> of symbols and their
         * quantities on the visible side of the card
         */
        HashMap<Symbol, Integer> getHashMapSymbols();

    }

    /**
     * This class implements the up state of the card
     */
    private class CardUp implements CardState{
        @Override
        public void swap(){
            cardState = new CardDown();
        }

        @Override
        public Corner getCorner(CornerPosition position){
            return frontGridConfiguration[position.getX()][position.getY()];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.UP;
        }

        @Override
        public boolean enoughResourceToBePlaced(HashMap<Symbol, Integer> freeResources){
            for(Symbol s : requiredSymbolToPlace.keySet()){
                if(freeResources.get(s) < requiredSymbolToPlace.get(s)){
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
            for(Corner[] row : frontGridConfiguration){
                for(Corner c : row){
                    c.getSymbol().ifPresent(s ->
                        symbolHashMap.compute(s, (k, v) -> v + 1));
                }
            }
            return symbolHashMap;
        }

    }

    /**
     * This class implements the up state of the card
     */
    private class CardDown implements CardState{
        @Override
        public void swap(){
            cardState = new CardUp();
        }

        @Override
        public Corner getCorner(CornerPosition position){
            return backGridConfiguration[position.getX()][position.getY()];
        }

        @Override
        public CardOrientation getState() {
            return CardOrientation.DOWN;
        }

        @Override
        public boolean enoughResourceToBePlaced(HashMap<Symbol, Integer> freeResources){
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
            for(Corner[] row : backGridConfiguration){
                for(Corner c : row){
                    c.getSymbol().ifPresent(s ->
                            symbolHashMap.compute(s, (k, v) -> v + 1));
                }
            }
            return symbolHashMap;
        }

    }

}