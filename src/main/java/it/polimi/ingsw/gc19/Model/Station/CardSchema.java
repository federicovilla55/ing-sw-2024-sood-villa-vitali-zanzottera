package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

import java.util.*;

public class CardSchema{
    private final PlayableCard[][] cardSchema;
    private final int[][] cardOverlap;
    private int currentCount;
    private final HashMap<PlayableCard, Tuple<Integer, Integer>> cardPosition;

    public CardSchema(){
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

    public boolean placeCard(){
        return true;
    }

    public Optional<PlayableCard> getCard(int x, int y) throws InvalidPositionException{
        if(checkCoords(x, y)){
            return Optional.ofNullable(this.cardSchema[x][y]);
        }
        else{
            throw new InvalidPositionException();
        }
    }

    public Tuple<Integer, Integer> getCoords(PlayableCard card) throws InvalidCardException{
        if(!this.cardPosition.containsKey(card)){
            throw new InvalidCardException();
        }
        return this.cardPosition.get(card);
    }

    public boolean cardOverAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException {
        Tuple<Integer, Integer> anchorCoords = getCoords(anchor);
        return this.cardOverlap[anchorCoords.x() + dir.getX()][anchorCoords.x() + dir.getY()] > this.cardOverlap[anchorCoords.x()][anchorCoords.y()];
    }

    public boolean cardOverAnchor(int x, int y, Direction dir) throws InvalidPositionException{
        return this.getCard(x, y).map(c -> {
            try {
                return cardOverAnchor(c, dir);
            } catch (InvalidCardException e) {
                return false;
            }
        }).orElse(false);
    }

    private boolean checkCoords(int x, int y){
        return (0 <= x) && (x < ImportantConstants.gridDimension) && (0 <= y) && (y < ImportantConstants.gridDimension);
    }

    public Optional<PlayableCard> getCardWithAnchor(PlayableCard anchor, Direction dir) throws InvalidAnchorException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        return Optional.ofNullable(this.cardSchema[coords.x() + dir.getX()][coords.y() + dir.getY()]);
    }

    public Optional<PlayableCard> getCardWithAnchor(int x, int y, Direction dir) throws InvalidPositionException{
        return this.getCard(x, y).flatMap(c -> {
            try{
                return getCardWithAnchor(c, dir);
            }catch(InvalidAnchorException e) {
                return Optional.empty();
            }
        });
    }

     void placeInitialCard(PlayableCard initialCard){
        if(initialCard.getCardType() == PlayableCardType.INITIAL){
            this.cardSchema[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = initialCard;
            this.cardOverlap[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = 1;
            this.cardPosition.put(initialCard, new Tuple<>(ImportantConstants.gridDimension / 2, ImportantConstants.gridDimension / 2));
        }
    }

    boolean isPlaceable(PlayableCard anchor, Direction direction) throws InvalidAnchorException{ //riportare in station
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        return this.cardSchema[this.cardPosition.get(anchor).x() + direction.getX()][this.cardPosition.get(anchor).y() + direction.getY()] == null;
    }

    boolean isPlaceable(int x, int y, Direction dir) throws InvalidPositionException{ //riportare nella classe station
        return this.getCard(x, y).map(c -> {
            try {
                return isPlaceable(c, dir);
            } catch (InvalidAnchorException e) {
                return false;
            }
        }).orElse(false);
    }

    void placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction){ //metto package visible perchè bisogna passare da station
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        this.cardSchema[coords.x() + direction.getX()][coords.y() + direction.getY()] = toPlace;
        this.cardOverlap[coords.x() + direction.getX()][coords.y() + direction.getY()] = this.currentCount + 1;
        this.currentCount++;
        this.cardPosition.put(toPlace, new Tuple<>(coords.x() + direction.getX(), coords.y() + direction.getY()));
    }

    public void placeCard(int x, int y, PlayableCard toPlace, Direction direction){
        this.cardSchema[x + direction.getX()][y + direction.getY()] = toPlace;
        this.cardOverlap[x + direction.getX()][y + direction.getY()] = this.currentCount + 1;
        this.currentCount++;
        this.cardPosition.put(toPlace, new Tuple<>(x + direction.getX(), y + direction.getY()));
    }

    public PlayableCard getLastPlaced(){
        Tuple<Integer, Integer> coords = null;
        int currMax = 0;
        for(int i = 0, k = 0; i < ImportantConstants.gridDimension && k < ImportantConstants.gridDimension; i++, k++){
            if(this.cardOverlap[i][k] > currMax){
                currMax = this.cardOverlap[i][k];
                coords = new Tuple<>(i, k);
            }
        }
        if(coords == null) return null;
        return this.cardSchema[coords.x()][coords.y()];
    }

    public boolean cardIsInSchema(PlayableCard card){
        return this.cardPosition.containsKey(card);
    }

    public Set<PlayableCard> cardsInSchema(){
        return this.cardPosition.keySet();
    }

    public String[][] getCardSchema() {
        return Arrays.stream(this.cardSchema)
                     .flatMap(Arrays::stream)
                     .map(Card::getCardCode)
                     .map(x -> new String[ImportantConstants.gridDimension])
                     .toArray(String[][]::new);
    }

    public int[][] getCardOverlap() {
        return Arrays.stream(this.cardOverlap).map(int[]::clone).toArray(int[][]::new);
    }

    public int countPattern(ArrayList<Tuple<Integer, Integer>> moves, ArrayList<Symbol> requiredSymbol){
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
                    while(found && numOfCard < moves.size() + 1){
                        currentX = currentX + moves.get(numOfCard - 1).x();
                        currentY = currentY + moves.get(numOfCard - 1).y();

                        if(checkCoords(currentX, currentY) && this.cardSchema[currentX][currentY] != null && !usedCards[i][k] && this.cardSchema[i][k].getSeed() == requiredSymbol.get(numOfCard - 1)){
                            numOfCard++;
                            cardInPattern.add(new Tuple<>(currentX, currentY));
                        }
                        else{
                            found = false;
                            for(Tuple<Integer, Integer> t : cardInPattern){
                                usedCards[t.x()][t.y()] = false;
                            }
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
}
