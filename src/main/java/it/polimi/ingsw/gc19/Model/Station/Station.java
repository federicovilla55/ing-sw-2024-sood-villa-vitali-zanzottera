package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Station{

    private final ArrayList<PlayableCard> cardsInStation;
    private final HashMap<Symbol, Integer> visibleSymbolsInStation;
    private GoalCard privateGoalCard;
    private int numPoints;
    private final CardSchema cardSchema;

    /**
     * This constructor creates a Station and its own card schema
     */
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

    /**
     * This method returns the number of visible symbols in this station
     * @return the hashmap of visible symbol - integer
     */
    public HashMap<Symbol, Integer> getVisibleSymbolsInStation(){
        return this.visibleSymbolsInStation;
    }

    /**
     * This method updates PlayableCard inside this station
     */
    public void updateCardsInHand(PlayableCard toInsert){
        this.cardsInStation.add(toInsert);
    }

    /**
     * This method returns station's current points
     * @return the current points of the station
     */
    public int getNumPoints(){
        return this.numPoints;
    }

    /**
     * This method returns station's private GoalCard
     * @return station's private GoalCard
     */
    public GoalCard getPrivateGoalCard(){
        return this.privateGoalCard;
    }

    /**
     * This method returns visible cards in this station
     * @return the ArrayList of visible cards in station
     */
    public ArrayList<PlayableCard> getCardsInStation(){
        return this.cardsInStation;
    }

    /**
     * This method sets private GoalCard of the station
     */
    public void setPrivateGoalCard(GoalCard privateGoalCard){
        this.privateGoalCard = privateGoalCard;
    }

    /**
     * This method updates station's points after card utilization
     */
    public void updatePoints(Card card){
        this.numPoints = this.numPoints + card.countPoints(this);
    }

    /**
     * This method place initial card in card schema
     */
    public void placeInitialCard(PlayableCard initialCard){
        if(initialCard.getCardType() == PlayableCardType.INITIAL){
            initialCard.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
            this.cardSchema.placeInitialCard(initialCard);
        }
    }

    /**
     * This method checks if a card can be placed in CardSchema
     * @return true if and only if card is in station and is placeable in CardSchema.
     * @throws InvalidCardException if station doesn't have the card to place.
     * @throws InvalidAnchorException if the anchor isn't in card schema.
     */
    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{
        if(!this.cardsInStation.contains(toPlace)){
            throw new InvalidCardException();
        }
        return this.cardSchema.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this.visibleSymbolsInStation);
    }

    /**
     * This method place a card in CardSchema if it's placeable in CardSchema
     * If the card has been placed it updates station's points and visible symbols. Then updates cards in hand.
     */
    public boolean placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{
        if(this.cardIsPlaceable(anchor, toPlace, direction)){
            this.cardsInStation.remove(toPlace);
            this.cardSchema.placeCard(anchor, toPlace, direction);
            toPlace.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
            for(Direction d : Direction.values()){
                try{
                    this.cardSchema.getCardWithAnchor(toPlace, d)
                                   .flatMap(x -> x.getCorner(d.getOtherCornerPosition()).getSymbol())
                                   .ifPresent(s -> this.visibleSymbolsInStation.compute(s, (k, v) -> v - 1));
                }
                catch(Exception ignored){};
            }
            updatePoints(toPlace);
            return true;
        }
        return false;
    }

    /**
     * This method returns visible cards in station
     * @return visible cards in station
     */
    public ArrayList<PlayableCard> getCardsInHand(){
        return this.cardsInStation;
    }

    /**
     * This method checks if there is a card over the anchor in Direction dir
     * Throws InvalidCardException is the anchor doesn't exist in schema.
     * @return true if and only if there is card ver the anchor in Direction dir
     */
    public boolean cardOverAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException{
        return this.cardSchema.cardOverAnchor(anchor, dir);
    }

    /**
     * This method returns an optional of card placed from the anchor in the specified direction;
     * optional is empty if this card doesn't exist.
     * @param anchor the anchor from which the method starts searching
     * @param dir the direction of the search
     * @return Optional<PlayableCard> describing the card
     */
    public Optional<PlayableCard> getCardWithAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException{
        return this.cardSchema.getCardWithAnchor(anchor, dir);
    }

    /**
     * This method returns an optional of card containing the last placed card;
     * optional is empty if no cards has been placed.
     * @return Optional<PlayableCard> describing the last placed card
     */
    public Optional<PlayableCard> getLastPlaced(){
        return this.cardSchema.getLastPlaced();
    }

    /**
     * This method checks if card has been placed
     * @param card the card to search for
     * @return true if and only if the card has been placed.
     */
    public boolean cardIsInSchema(PlayableCard card){
        return this.cardSchema.cardIsInSchema(card);
    }

    /**
     * This method returns the schema where each card is described by its name
     * Optional is empty if in (x, y) there is no card
     * @return a String matrix with all the visible cards codes.
     */
    public Optional<String>[][] getCardCodeSchema(){
        return this.cardSchema.getCardSchema();
    }

    /**
     * This method returns the orientation of each card in the schema
     * Optional is empty if in (x, y) there is no card
     * @return a String matrix with all the visible cards codes.
     */
    public Optional<CardOrientation>[][] getCardOrientationSchema(){
        return this.cardSchema.getCardOrientation();
    }

    /**
     * This method returns a matrix with 0 if in (x, y) there's no card otherwise a sequential
     * number representing the order of card placing.
     * If getCardOverlap()[x][y] > getCardOverlap()[i][j] then card in position [x][y] has been placed
     * after card in position [i][j]
     * @return an integer matrix representing current overlapping situation
     */
    public int[][] getCardOverlap(){
        return this.cardSchema.getCardOverlap();
    }

    /**
     * This method returns the number of specified pattern in card schema
     * @param moves the Arraylist of movement inside card schema
     * @param requiredSymbol the ArrayList of required seeds of each card in the pattern
     * @return the number of the specified pattern found
     */
    public int countPattern(ArrayList<Tuple<Integer, Integer>> moves, ArrayList<Symbol> requiredSymbol){
        return this.cardSchema.countPattern(moves, requiredSymbol);
    }

}
