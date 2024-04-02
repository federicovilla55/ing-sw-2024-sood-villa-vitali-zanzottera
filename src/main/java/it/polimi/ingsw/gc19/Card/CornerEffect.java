package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

public class CornerEffect implements PlayableEffect{

    private final int cardValue;

    protected CornerEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station, Card card){
        //int x = station.getX(card);
        return 0;
    }

    @Override
    public String getEffectDescription(){
        return " 2 points for every corner that this card now hides";
    }

}
