package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

public class CornerEffect implements PlayableEffect{

    private final int cardValue;

    protected CornerEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        //TODO: implement countPoints method
        return 0;
    }

    @Override
    public String getEffectDescription(){
        return " 2 points for every corner that this card now hides";
    }

}
