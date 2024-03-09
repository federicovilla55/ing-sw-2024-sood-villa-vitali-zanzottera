package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.Arrays;
import java.util.Optional;

class CornerEffect implements PlayableEffect{

    private final int cardValue;

    protected CornerEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        int numCop = 0;
        for(Direction d : Direction.values()){
            try {
                station.getCardSchema().getCardWithAnchor(station.getCardSchema().getLastPlaced(), d);
                numCop++;
            }catch(Exception ignored){};
        }
        return numCop * this.cardValue;
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "for every corner that this card hides";
    }

}
