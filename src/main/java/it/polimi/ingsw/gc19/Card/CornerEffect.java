package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Station.Station;

public class CornerEffect implements PlayableEffect{

    private final int cardValue;

    protected CornerEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station, PlayableCard card){
        int numCop = 0;
        for(Direction d :Direction.getCornerDirection()){
            try{
                station.getCardWithAnchor(card, d);
                numCop++;
            }
            catch(InvalidCardException | NullPointerException | InvalidAnchorException exception) {};
        }
        return numCop * this.cardValue;
    }

    @Override
    public String getEffectDescription(){
        return this.cardValue + " points for every corner that this card now hides";
    }

}
