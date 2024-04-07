package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlaceCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlaceInitialCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;

import java.util.*;

public class Station extends Publisher{

    private final Player ownerPlayer;

    private final ArrayList<PlayableCard> cardsInHand;
    private final Map<Symbol, Integer> visibleSymbolsInStation;
    private Integer privateGoalCardIdx;
    private int numPoints;
    private int pointsFromGoals;

    private boolean initialCardIsPlaced;
    private final CardSchema cardSchema;

    private final PlayableCard initialCard;
    private final GoalCard[] privateGoalCardsInStation;

    /**
     * This constructor creates a Station and its own card schema
     */
    public Station(Player ownerPlayer, PlayableCard initialCard, GoalCard privateGoalCard1, GoalCard privateGoalCard2){
        super();
        this.ownerPlayer = ownerPlayer;
        this.numPoints = 0;
        this.initialCardIsPlaced = false;
        this.privateGoalCardIdx = null;
        this.visibleSymbolsInStation = new HashMap<>();
        this.initialCard = initialCard;
        this.privateGoalCardsInStation = new GoalCard[]{privateGoalCard1,privateGoalCard2};

        for(Symbol s : Symbol.values()){
            this.visibleSymbolsInStation.put(s, 0);
        }

        this.cardsInHand = new ArrayList<>();

        this.cardSchema = new CardSchema();

    }

    /**
     * This method returns the number of visible symbols in this station
     * @return the hashmap of visible symbol - integer
     */
    public Map<Symbol, Integer> getVisibleSymbolsInStation(){
        return this.visibleSymbolsInStation;
    }

    /**
     * This method updates PlayableCard inside this station
     */
    public void updateCardsInHand(PlayableCard toInsert){
        this.cardsInHand.add(toInsert);
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
        if (privateGoalCardIdx==null) return null;
        return this.privateGoalCardsInStation[privateGoalCardIdx];
    }

    /**
     * This method sets private GoalCard of the station
     */
    public void setPrivateGoalCard(int cardIdx) {
        this.privateGoalCardIdx = cardIdx;
        this.getMessageFactory().sendMessageToPlayer(this.ownerPlayer.getName(), new AcceptedChooseGoalCard(this.getPrivateGoalCard()));
    }

    /**
     * This method updates station's points after card utilization
     * @param card the card to use to update points
     * @return the points given by the card
     */
    public int updatePoints(Card card){
        int pointsFromCard = card.countPoints(this);
        this.numPoints = this.numPoints + pointsFromCard;
        return pointsFromCard;
    }

    /**
     * This method place initial card in card schema
     */
    public void placeInitialCard(PlayableCard initialCard, CardOrientation cardOrientation){
        if(initialCard.getCardType() == PlayableCardType.INITIAL && !this.initialCardIsPlaced){
            initialCard.setCardState(cardOrientation);
            initialCard.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
            this.cardSchema.placeInitialCard(initialCard);
            this.initialCardIsPlaced  = true;
            //Message
            this.getMessageFactory().sendMessageToAllGamePlayers(new AcceptedPlaceInitialCard(this.ownerPlayer.getName(),
                                                                                              initialCard, Map.copyOf(this.visibleSymbolsInStation)));
            //Message
        }
    }

    /**
     * This method place initial card in card schema
     */
    public void placeInitialCard(CardOrientation cardOrientation){
        this.placeInitialCard(this.initialCard, cardOrientation);
    }

    /**
     * This method checks if a card can be placed in CardSchema
     * @return true if and only if card is in station and is placeable in CardSchema.
     * @throws InvalidCardException if station doesn't have the card to place.
     * @throws InvalidAnchorException if the anchor isn't in card schema.
     */
    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard toPlace, Direction direction) throws InvalidCardException, InvalidAnchorException{
        if(!this.getCardsInHand().contains(toPlace)){
            throw new InvalidCardException();
        }
        if(!toPlace.enoughResourceToBePlaced(this.visibleSymbolsInStation)){
            this.getMessageFactory().sendMessageToPlayer(this.ownerPlayer.getName(),
                                                         new RefusedActionMessage(ErrorType.GENERIC, "Attention, you haven't enough resources to place card " + toPlace.getCardCode()));
        }
        if(!this.cardSchema.isPlaceable(anchor, direction)){
            this.getMessageFactory().sendMessageToPlayer(this.ownerPlayer.getName(),
                                                         new RefusedActionMessage(ErrorType.GENERIC,
                                                                                  "Attention, " + toPlace.getCardCode() + " cannot be placed over anchor " + anchor.getCardCode() + " in direction " + direction));
        }
        return this.cardSchema.isPlaceable(anchor, direction) && toPlace.enoughResourceToBePlaced(this.visibleSymbolsInStation);
    }

    /**
     * This method place a card in CardSchema if it's placeable in CardSchema
     * If the card has been placed it updates station's points and visible symbols. Then updates cards in hand.
     * @throws InvalidCardException if station doesn't have the card to place.
     * @throws InvalidAnchorException if the anchor isn't in card schema.
     */
    public boolean placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction, CardOrientation cardOrientation) throws InvalidCardException, InvalidAnchorException{
        toPlace.setCardState(cardOrientation);
        if(this.cardIsPlaceable(anchor, toPlace, direction)){
            this.cardsInHand.remove(toPlace);
            this.cardSchema.placeCard(anchor, toPlace, direction);
            this.cardsInHand.remove(toPlace);
            setVisibleSymbols(toPlace);
            updatePoints(toPlace);
            //Message
            this.getMessageFactory().sendMessageToAllGamePlayers(new AcceptedPlaceCardMessage(this.ownerPlayer.getName(),
                                                                                              anchor.getCardCode(), toPlace, direction,
                                                                                              Map.copyOf(this.visibleSymbolsInStation), this.numPoints));
            //Message
            return true;
        }
        return false;
    }

    private void setVisibleSymbols(PlayableCard toPlace) {
        toPlace.getHashMapSymbols().forEach((k, v) -> this.visibleSymbolsInStation.merge(k, v, Integer::sum));
        for(Direction d : Direction.values()){
            try{
                this.cardSchema.getCardWithAnchor(toPlace, d)
                               .flatMap(x -> x.getCorner(d.getOtherCornerPosition()).getSymbol())
                               .ifPresent(s -> this.visibleSymbolsInStation.compute(s, (k, v) -> v - 1));
            }
            catch(Exception ignored){};
        }
    }

    /**
     * This method returns visible cards in station
     * @return visible cards in station
     */
    public ArrayList<PlayableCard> getCardsInHand(){
        return this.cardsInHand;
    }

    /**
     * This method checks if there is a card over the anchor in Direction dir
     * Throws InvalidCardException is the anchor doesn't exist in schema.
     * @throws InvalidCardException if station doesn't have the card to place.
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
     * @throws InvalidCardException if station doesn't have the card to place.
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

    public GoalCard getPrivateGoalCardInStation(int cardIdx) {
        return privateGoalCardsInStation[cardIdx];
    }

    public boolean getInitialCardIsPlaced() {
        return initialCardIsPlaced;
    }

    public PlayableCard getInitialCard() {
        return initialCard;
    }

    public int getPointsFromGoals() {
        return pointsFromGoals;
    }

    public void setPointsFromGoals(int pointsFromGoals) {
        this.pointsFromGoals = pointsFromGoals;
    }

    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        return this.cardSchema.getPlacedCardSequence();
    }
}
