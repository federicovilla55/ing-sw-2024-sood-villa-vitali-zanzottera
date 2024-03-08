package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

import java.util.HashMap;
import java.util.Set;

public class CardSchema{
    private final PlayableCard[][] cardSchema;
    private final HashMap<PlayableCard, Tuple<Integer, Integer>> cardPosition;

    public CardSchema(){
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        this.cardPosition = new HashMap<>();
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
        return getCoords(card).getX();
    }

    public int getY(PlayableCard card) throws InvalidCardException{
        return getCoords(card).getY();
    }

    public boolean checkCoords(int x, int y){
        return (0 <= x) && (x < ImportantConstants.gridDimension) && (0 <= y) && (y < ImportantConstants.gridDimension);
    }

    public PlayableCard getCardWithAnchor(PlayableCard anchor, Direction dir) throws NoCardException, InvalidAnchorException, InvalidPositionException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        if(!checkCoords(coords.getX() + dir.getX(), coords.getY() + dir.getY())){
            throw new InvalidPositionException();
        }
        if(this.cardSchema[coords.getX()][coords.getY()] == null){
            throw new NoCardException();
        }
        return this.cardSchema[coords.getX()][coords.getY()];
    }

    public boolean isPlaceable(PlayableCard anchor, Direction direction) throws InvalidAnchorException, InvalidPositionException{
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidAnchorException();
        }
        if(!checkCoords(this.cardPosition.get(anchor).getX() + direction.getX(), this.cardPosition.get(anchor).getY() + direction.getY())){
            throw new InvalidPositionException();
        }
        return this.cardSchema[this.cardPosition.get(anchor).getX() + direction.getX()][this.cardPosition.get(anchor).getY() + direction.getY()] == null;
    }

    public void placeCard(PlayableCard anchor, PlayableCard toPlace, Direction direction){
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        this.cardSchema[coords.getX() + direction.getX()][coords.getY() + direction.getY()] = toPlace;
        this.cardPosition.put(toPlace, new Tuple<>(coords.getX() + direction.getX(), coords.getY() + direction.getY()));
    }

    public boolean cardIsInSchema(PlayableCard card){
        return this.cardPosition.containsKey(card);
    }

    public Set<PlayableCard> cardsInSchema(){
        return this.cardPosition.keySet();
    }

    public Card[][] getCardSchema() {
        return this.cardSchema;
    }

}
