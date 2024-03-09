package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Station.Station;

class CornerEffect implements PlayableEffect{

    private final int cardValue;
    private final PlayableCard cardAttached;

    protected CornerEffect(int cardValue, PlayableCard cardAttached){
        this.cardValue = cardValue;
        this.cardAttached = cardAttached;
    }

    @Override
    public int countPoints(Station station){
        int numCop = 0;
        for(Direction d : Direction.values()){
            try{
                station.getCardSchema().getCardWithAnchor(this.cardAttached, d);
                numCop++;
            }
            catch(Exception ignored){};
        }
        return numCop * this.cardValue;
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "for every corner that this card hides";
    }

}
