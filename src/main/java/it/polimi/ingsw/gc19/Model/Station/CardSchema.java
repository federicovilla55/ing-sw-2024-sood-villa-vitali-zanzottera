package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.*;

class CardSchema{
    private final PlayableCard[][] cardSchema;
    private final int[][] cardOverlap;
    private int currentCount;
    private final HashMap<PlayableCard, Tuple<Integer, Integer>> cardPosition;

    /**
     * This constructor builds a card schema instance
     */
    CardSchema(){
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        this.cardOverlap = new int[ImportantConstants.gridDimension][ImportantConstants.gridDimension];

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                this.cardSchema[i][k] = null;
                this.cardOverlap[i][k] = 0;
            }
        }

        this.cardPosition = new HashMap<>();
        this.currentCount = 0;
    }

    /**
     * This method checks if there is a card over the anchor in Direction dir
     * Throws InvalidCardException is the anchor doesn't exist in schema.
     * @throws InvalidCardException if station doesn't have the card to place.
     * @return true if and only if there is card ver the anchor in Direction dir
     */
    boolean cardOverAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidCardException();
        }
        Tuple<Integer, Integer> anchorCoords = this.cardPosition.get(anchor);
        return this.cardOverlap[anchorCoords.x() + dir.getX()][anchorCoords.x() + dir.getY()] > this.cardOverlap[anchorCoords.x()][anchorCoords.y()];
    }

    /**
     * This method checks if (x, y) are available coords inside the matrix
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if and only if (x, y) are valid coordinates
     */
    private boolean checkCoords(int x, int y){
        return (0 <= x) && (x < ImportantConstants.gridDimension) && (0 <= y) && (y < ImportantConstants.gridDimension);
    }

    /**
     * This method returns an optional of card placed from the anchor in the specified direction;
     * optional is empty if this card doesn't exist.
     * @param anchor the anchor from which the method starts searching
     * @param dir the direction of the search
     * @throws InvalidCardException if station doesn't have the card to place.
     * @return Optional<PlayableCard> describing the card
     */
    Optional<PlayableCard> getCardWithAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidCardException();
        }
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        return Optional.ofNullable(this.cardSchema[coords.x() + dir.getX()][coords.y() + dir.getY()]);
    }

    /**
     * This method place initial card in CardSchema
     */
     void placeInitialCard(PlayableCard initialCard){
         this.cardSchema[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = initialCard;
         this.cardOverlap[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = 1;
         this.currentCount++;
         this.cardPosition.put(initialCard, new Tuple<>(ImportantConstants.gridDimension / 2, ImportantConstants.gridDimension / 2));
    }

    /**
     * This method checks if a card (even if it isn't visible in the station) can be placed in CardSchema
     * @throws InvalidAnchorException if the anchor isn't in card schema.
     * @return true if and only if card is in station and is placeable in CardSchema.
     */
    boolean isPlaceable(PlayableCard anchor, Direction direction) throws InvalidAnchorException{
        PlayableCard neighborCard;
        int currentX, currentY;
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        currentX = this.cardPosition.get(anchor).x() + direction.getX();
        currentY = this.cardPosition.get(anchor).y() + direction.getY();
        if(this.cardSchema[currentX][currentY] != null){
            return false;
        }

        for(Direction dir : Direction.values()){
            neighborCard = this.cardSchema[currentX + dir.getX()][currentY + dir.getY()];
            if(neighborCard != null && !neighborCard.canPlaceOver(dir.getOtherCornerPosition())){
                return false;
            }
        }
        return true;
    }

    /**
     * This method place a card in CardSchema if it's placeable in CardSchema
     * If the card has been placed it updates station's points and visible symbols. Then updates cards in hand.
     */
    void placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction){
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        this.cardSchema[coords.x() + direction.getX()][coords.y() + direction.getY()] = toPlace;
        this.cardOverlap[coords.x() + direction.getX()][coords.y() + direction.getY()] = this.currentCount + 1;
        this.currentCount++;
        this.cardPosition.put(toPlace, new Tuple<>(coords.x() + direction.getX(), coords.y() + direction.getY()));
    }

    /**
     * This method returns an optional of card containing the last placed card;
     * optional is empty if no cards has been placed.
     * @return Optional<PlayableCard> describing the last placed card
     */
    Optional<PlayableCard> getLastPlaced(){
        Tuple<Integer, Integer> coords = null;
        int currMax = 0;
        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++) {
                if (this.cardOverlap[i][k] > currMax) {
                    currMax = this.cardOverlap[i][k];
                    coords = new Tuple<>(i, k);
                }
            }
        }
        if(coords == null) return Optional.empty();
        return Optional.of(this.cardSchema[coords.x()][coords.y()]);
    }

    /**
     * This method checks if card has been placed
     * @param card the card to search for
     * @return true if and only if the card has been placed.
     */
    boolean cardIsInSchema(PlayableCard card){
        return this.cardPosition.containsKey(card);
    }

    /**
     * This method returns the schema where each card is described by its name
     * Optional is empty if in (x, y) there is no card
     * @return a String matrix with all the visible cards codes.
     */
    Optional<String>[][] getCardSchema(){
        Optional<String>[][] matrixToReturn = new Optional[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                matrixToReturn[i][k] = Optional.ofNullable(this.cardSchema[i][k]).map(PlayableCard::getCardCode);
            }
        }
        return matrixToReturn;
    }

    /**
     * This method returns orientation of each card in the schema.
     * Optional is empty if in (x, y) there is no card
     * @return a String matrix with all the visible cards codes.
     */
    Optional<CardOrientation>[][] getCardOrientation() {
        Optional<CardOrientation>[][] matrixToReturn = new Optional[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                matrixToReturn[i][k] = Optional.ofNullable(this.cardSchema[i][k].getCardOrientation());
            }
        }
        return matrixToReturn;
    }

    /**
     * This method returns a matrix with 0 if in (x, y) there's no card otherwise a sequential
     * number representing the order of card placing.
     * If getCardOverlap()[x][y] > getCardOverlap()[i][j] then card in position [x][y] has been placed
     * after card in position [i][j]
     * @return an integer matrix representing current overlapping situation
     */
    int[][] getCardOverlap() {
        return Arrays.stream(this.cardOverlap).map(int[]::clone).toArray(int[][]::new);
    }

    /**
     * This method returns the number of specified pattern in card schema
     * @param moves the Arraylist of movement inside card schema
     * @param requiredSymbol the ArrayList of required seeds of each card in the pattern
     * @return the number of the specified pattern found
     */
    int countPattern(ArrayList<Tuple<Integer, Integer>> moves, ArrayList<Symbol> requiredSymbol){
        boolean[][] usedCards = new boolean[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        ArrayList<Tuple<Integer, Integer>> cardInPattern = new ArrayList<>();
        boolean found;
        int currentX, currentY;
        int numOfCard;
        int numOfPattern = 0;

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k =0; k < ImportantConstants.gridDimension; k++){
                usedCards[i][k] = false;
            }
        }

        usedCards[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = true;

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k <ImportantConstants.gridDimension; k++){
                if(this.cardSchema[i][k] != null && this.cardSchema[i][k].getSeed() == requiredSymbol.getFirst() && !usedCards[i][k]){
                    found = true;
                    currentX = i;
                    currentY = k;
                    numOfCard = 1;
                    cardInPattern.clear();
                    cardInPattern.add(new Tuple<>(currentX, currentY));
                    while(found && numOfCard < moves.size() + 1){
                        currentX = currentX + moves.get(numOfCard - 1).x();
                        currentY = currentY + moves.get(numOfCard - 1).y();

                        if(checkCoords(currentX, currentY) && this.cardSchema[currentX][currentY] != null && !usedCards[currentX][currentY] && this.cardSchema[currentX][currentY].getSeed() == requiredSymbol.get(numOfCard)){
                            numOfCard++;
                            cardInPattern.add(new Tuple<>(currentX, currentY));
                        }
                        else{
                            found = false;
                        }
                    }

                    if(numOfCard == moves.size() + 1){
                        for(Tuple<Integer, Integer> t : cardInPattern){
                            usedCards[t.x()][t.y()] = true;
                        }
                        numOfPattern++;
                    }
                }
            }
        }

        return numOfPattern;

    }

    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        List<Tuple<PlayableCard, Tuple<Integer, Integer>>> res = new ArrayList<>(
                Arrays.stream(this.cardOverlap).flatMapToInt(Arrays::stream).max().orElse(0)
        );
        for(int i = 0; i < ImportantConstants.gridDimension; i++) {
            for(int j = 0; j < ImportantConstants.gridDimension; j++) {
                if(cardOverlap[i][j] > 0) {
                    res.add(cardOverlap[i][j]-1, new Tuple<>(this.cardSchema[i][j], new Tuple<>(i,j)));
                }
            }
        }
        return res;
    }
}
