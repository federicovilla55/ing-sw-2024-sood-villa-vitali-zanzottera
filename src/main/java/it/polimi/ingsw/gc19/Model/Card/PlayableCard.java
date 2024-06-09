package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.*;
import it.polimi.ingsw.gc19.Enums.*;

import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class represents a single playable card
 */
@JsonTypeName("playable")
public class PlayableCard extends Card implements Serializable{

    /**
     * This attribute represents card type: Initial, Resource or Gold
     */
    private PlayableCardType cardType;

    /**
     * This attribute represents a 2x2 matrix used to store
     * information about the four front corners of the card.
     * A corner can be {@link NotAvailableCorner}, {@link EmptyCorner} or can contain a
     * {@link Symbol}
     */
    private Corner[][] frontGridConfiguration;

    /** Symbols and quantity that a player needs to have to
     * place the card in the {@link CardOrientation#UP} state in his station.
     */
    private HashMap<Symbol, Integer> requiredSymbolToPlace;

    /**
     * This attribute represents a 2x2 matrix used to store
     * information about the four back corners of the card.
     * A corner can be {@link NotAvailableCorner}, {@link EmptyCorner} or can contain a
     * {@link Symbol}
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
     * implements {@link PlayableEffect}
     */
    private PlayableEffect playableEffect;

    @JsonCreator
    public PlayableCard(
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

    /**
     * This method returns the type of the card
     * @return {@link #cardType}
     */
    public PlayableCardType getCardType(){
        return this.cardType;
    }

    /**
     * This method returns a specified card corner given a position
     * @param position a position among {@link CornerPosition}
     * @return {@link Corner} type of specified corner
     */
    public Corner getCorner(CornerPosition position){
        return this.cardState.getCorner(position);
    }

    /**
     * Checks if, depending on {@link #cardState}, {@param freeResources} contains
     * sufficient resources to place the card
     * @param freeResources the <code>Map&lt;Symbol, Integer&gt;</code> to check
     * @return <code>true</code> if and only there are enough resources to place
     * the card
     */
    public boolean enoughResourceToBePlaced(Map<Symbol, Integer> freeResources){
        return this.cardState.enoughResourceToBePlaced(freeResources);
    }

    /**
     * This method returns true if and only if the specified corner is valid to
     * place a card on top of it
     * @param position a position among {@link CardOrientation}
     * @return true if and only if the corner of chosen position is different
     * from {@link NotAvailableCorner}
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
     * @return <code>ArrayList&lt;Symbol&gt;</code> of resources in the center
     * of the card (it is always empty when the card is on front)
     */
    public List<Symbol> getPermanentResources(){
        return this.cardState.getPermanentResources();
    }

    /**
     * This method computes symbols on the visible side of the card
     * depending on its state
     * @return <code>HashMap&lt;Symbol, Integer&gt;</code> of symbols and their
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

    /**
     * Setter for {@link #cardState}. Sets how card is orientated
     * @param cardOrientation the new {@link CardOrientation} of the card
     * @return the modified card
     */
    public PlayableCard setCardState(CardOrientation cardOrientation) {
        this.cardState = (cardOrientation == CardOrientation.DOWN ) ? new CardDown() : new CardUp();
        return this;
    }

    /**
     * Getter for string description of the card
     * @return the string description of the card
     */
    @Override
    public String getCardDescription(){
        StringBuffer desc = new StringBuffer();

        desc.append("Playable card ").append(this.getCardCode()).append(":\n");
        if(!this.requiredSymbolToPlace.isEmpty())
            desc.append("Required symbols to place: ").append(requiredSymbolToPlace.entrySet().stream().flatMap(entry -> Stream.generate(entry::getKey).limit(entry.getValue())).map(s -> s.stringEmoji()).reduce(String::concat).orElse("nothing")).append("\n");

        desc.append(this.playableEffect.getEffectDescription()).append("\n");

        return desc.toString();
    }

    /**
     * This method returns {@link CardOrientation#UP} or {@link CardOrientation#DOWN}
     * if the card is either in a state or the other
     * @return {@link CardOrientation#UP} if cardState is dynamically UP,
     * or {@link CardOrientation#DOWN} if cardState is dynamically DOWN
     */
    public CardOrientation getCardOrientation(){
        return cardState.getState();
    }

    /**
     * This method returns the Symbol describing card's seed
     * @return a valid Symbol if <code>cardType != PlayableCardType.INITIAL</code>,
     * else <code>null</code>
     */
    public Symbol getSeed(){
        if(this.cardType != PlayableCardType.INITIAL){
            return this.permanentResources.getFirst();
        }
        return null;
    }

    /**
     * Overriding of {@link Object#equals(Object)} for {@link Card}.
     * Two card objects are equals if and only if their card code are equal
     * @param obj the {@link Object} to compare
     * @return <code>true</code> if and ony if <code>obj</code> is a {@link Card},
     * the card codes are equals and the {@link CardOrientation} are the same
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj instanceof PlayableCard playableCardObj){
            return playableCardObj.getCardCode().equals(this.getCardCode()) && playableCardObj.getCardOrientation() == this.getCardOrientation();
        }
        return false;
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
         * @param position a position among {@link CornerPosition}
         * @return {@link Corner} type of specified corner
         */
        Corner getCorner(CornerPosition position);

        /**
         * This method returns a boolean describing whether card has enough resources to be placed in station
         * @param freeResources is the hashmap of free resources in card schema
         * @return boolean
         */
        boolean enoughResourceToBePlaced(Map<Symbol, Integer> freeResources);

        /**
         * This method returns the points gained by the card effect
         * after its placement in a station. If the card is on the back,
         * this method returns 0.
         * @param station the station where the card is placed
         * @return the points the card gives based on its effect
         * when placed
         */
        int countPoints(Station station);

        /**
         * This method returns true if and only if the specified corner is valid to
         * place a card on top of it
         * @param position a position among {@link CornerPosition}
         * @return true if and only if the corner of chosen position is different
         * from {@link NotAvailableCorner}
         */
        boolean canPlaceOver(CornerPosition position);

        /**
         * This method returns {@link CardOrientation#UP} or {@link CardOrientation#DOWN}
         * if the card is either in a state or the other
         * @return {@link CardOrientation#UP} if cardState is dynamically UP,
         * or {@link CardOrientation#DOWN} if cardState is dynamically DOWN
         */
        CardOrientation getState();

        /**
         * This method returns permanent resources on the visible side
         * of the card depending on its state
         * @return ArrayList&lt;Symbol&gt; of resources in the center
         * of the card (it is always empty when the card is on front)
         */
        List<Symbol> getPermanentResources();

        /**
         * This method computes symbols on the visible side of the card
         * depending on its state
         * @return HashMap&lt;Symbol, Integer&gt; of symbols and their
         * quantities on the visible side of the card
         */
        HashMap<Symbol, Integer> getHashMapSymbols();

    }

    /**
     * This class implements the up state of the card
     */
    private class CardUp implements CardState, Serializable{

        /**
         * Swaps card setting its {@link #cardState} to {@link CardOrientation#DOWN}
         */
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
        public boolean enoughResourceToBePlaced(Map<Symbol, Integer> freeResources){
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
        public List<Symbol> getPermanentResources() {
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
    private class CardDown implements CardState, Serializable{

        /**
         * Swaps card setting its {@link #cardState} to {@link CardOrientation#UP}
         */
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

        /**
         *
         * @param freeResources is the hashmap of free resources in card schema
         * @return always <code>true</code> because in {@link CardOrientation#DOWN}
         * cards can always be placed
         */
        @Override
        public boolean enoughResourceToBePlaced(Map<Symbol, Integer> freeResources){
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
        public List<Symbol> getPermanentResources() {
            return List.copyOf(permanentResources);
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

    /**
     * Getter for string description of the {@link PlayableCard}
     * @return a {@link String} description of the {@link PlayableCard}
     */
    @Override
    public String toString() {
        return super.toString() +
                "\n" +
                "Card orientation: " + this.getCardOrientation();
    }

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about card
     * @return TUI-view visual description of the effect of the card
     */
    public String[][] getEffectView(TUIView tuiView) {
        return this.playableEffect.getEffectView(tuiView);
    }
}