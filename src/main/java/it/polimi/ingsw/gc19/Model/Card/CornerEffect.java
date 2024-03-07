package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Model.Station.Station;

public class CornerEffect implements PlayableEffect{

    private final int cardValue;

    protected CornerEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station, PlayableCard card){
        int numCop = 0;
        for(Direction d : Direction.values()){
            try{
                station.getCardSchema().getCardWithAnchor(card, d);
                numCop++;
            }
            catch(Exception ignored){};
        }
        return numCop * this.cardValue;
    }

    @Override
    public String getEffectDescription(){
        return this.cardValue + " points for every corner that this card now hides";
    }

}
