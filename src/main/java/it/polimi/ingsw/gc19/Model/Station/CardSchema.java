package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Tuple.Tuple;

import java.util.HashMap;

public class CardSchema{
    private final Card[][] cardSchema;
    private final HashMap<Card, Tuple<Integer, Integer>> cardPosition;

    public CardSchema(){
        this.cardSchema = new Card[40][40];
        this.cardPosition = new HashMap<>();
    }

    public Card getCard(int x, int y) throws InvalidAxisException, InvalidCardException{
        if((x < 40) && (y < 40)) {
            if (this.cardSchema[x][y] == null) {
                throw new InvalidCardException();
            }
            return this.cardSchema[x][y];
        }
        else{
            throw new InvalidAxisException();
        }
    }

    public int getX(Card card) throws NullPointerException, InvalidCardException{
        if(card == null){
            throw new NullPointerException();
        }
        if(!this.cardPosition.containsKey(card)){
            throw new InvalidCardException();
        }
        return this.cardPosition.get(card).getX();
    }

    public int getY(Card card) throws NullPointerException, InvalidCardException{
        if(card == null){
            throw new NullPointerException();
        }
        if(!this.cardPosition.containsKey(card)){
            throw new InvalidCardException();
        }
        return this.cardPosition.get(card).getY();
    }

    public boolean checkCoords(int x, int y){
        return (0 <= x) && (x < 40) && (0 <= y) && (y < 40);
    }

    public Card getCardWithAnchor(PlayableCard anchor, Direction dir) throws InvalidCardException, InvalidAnchorException, NullPointerException{
        if((anchor == null) || (dir == null)){
            throw new NullPointerException();
        }
        if(!this.cardPosition.containsKey(anchor)){
            throw new InvalidCardException();
        }
        Tuple<Integer, Integer> coords = this.cardPosition.get(anchor);
        if(!checkCoords(coords.getX(), coords.getY())){
            throw new InvalidAnchorException();
        }
        if(this.cardSchema[coords.getX()][coords.getY()] == null){
            throw new InvalidCardException();
        }
        return this.cardSchema[coords.getX()][coords.getY()];

    }

    public boolean isPlaceable(PlayableCard anchor, Direction direction){
        return false;
    }

    public Card[][] getCardSchema() {
        return this.cardSchema;
    }

}
