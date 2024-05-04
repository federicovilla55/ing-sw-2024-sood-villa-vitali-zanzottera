package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

@JsonTypeName("corner")
class CornerEffect implements PlayableEffect{

    private final int cardValue;

    /**
     * This constructor creates a corner effect
     * @param cardValue the points associated to the pattern
     */
    protected CornerEffect(@JsonProperty("value") int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        int numCop = 0;
        for(Direction d : Direction.values()){
            try {
                if(station.getLastPlaced().isPresent() && station.getCardWithAnchor(station.getLastPlaced().get(), d).isPresent()){
                    numCop++;
                }
            }catch(Exception ignored){};
        }
        return numCop * this.cardValue;
    }

    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return new String[][]{{""}};
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "for every corner that this card covers";
    }

}
