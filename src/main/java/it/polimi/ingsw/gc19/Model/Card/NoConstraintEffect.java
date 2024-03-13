package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.gc19.Model.Station.Station;

class NoConstraintEffect implements PlayableEffect{
    private final int cardValue;

    NoConstraintEffect(@JsonProperty("value") int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        return cardValue;
    }

    @Override
    public String getEffectDescription() {
        return "Points per pattern: " + this.cardValue +
               "Required pattern: nothing";
    }

}
