package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

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

    public PlayableCard getCard(int x, int y) throws InvalidPositionException, NoCardException{
        if(checkCoords(x, y)){
            if(this.cardSchema[x][y] == null){
                throw new NoCardException();
            }
            return this.cardSchema[x][y];
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

    public int getX(PlayableCard card) throws InvalidCardException{
        return getCoords(card).x();
    }

    public int getY(PlayableCard card) throws InvalidCardException{
        return getCoords(card).y();
    }

    public boolean cardOverAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException {
        Tuple<Integer, Integer> anchorCoords = getCoords(anchor);
        return this.cardOverlap[anchorCoords.x() + dir.getX()][anchorCoords.x() + dir.getY()] > this.cardOverlap[anchorCoords.x()][anchorCoords.y()];
    }

    public boolean checkCoords(int x, int y){
        return (0 <= x) && (x < ImportantConstants.gridDimension) && (0 <= y) && (y < ImportantConstants.gridDimension);
    }

    public PlayableCard getCardWithAnchor(PlayableCard anchor, Direction dir) throws NoCardException, InvalidAnchorException, InvalidPositionException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        if(!checkCoords(coords.x() + dir.getX(), coords.y() + dir.getY())){
            throw new InvalidPositionException();
        }
        if(this.cardSchema[coords.x()][coords.y()] == null){
            throw new NoCardException();
        }
        return this.cardSchema[coords.x()][coords.y()];
    }

    public boolean isPlaceable(PlayableCard anchor, Direction direction) throws InvalidAnchorException, InvalidPositionException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        if(!checkCoords(this.cardPosition.get(anchor).x() + direction.getX(), this.cardPosition.get(anchor).y() + direction.getY())){
            throw new InvalidPositionException();
        }
        return this.cardSchema[this.cardPosition.get(anchor).x() + direction.getX()][this.cardPosition.get(anchor).y() + direction.getY()] == null;
    }

    public void placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction){
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        this.cardSchema[coords.x() + direction.getX()][coords.y() + direction.getY()] = toPlace;
        this.cardOverlap[coords.x() + direction.getX()][coords.y() + direction.getY()] = this.currentCount + 1;
        this.currentCount++;
        this.cardPosition.put(toPlace, new Tuple<>(coords.x() + direction.getX(), coords.y() + direction.getY()));
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

    public Card[][] getCardSchema() {
        return this.cardSchema; //implement deep copy
    }

    public int[][] getCardOverlap() {
        return Arrays.stream(this.cardOverlap).map(int[]::clone).toArray(int[][]::new);
    }
}
