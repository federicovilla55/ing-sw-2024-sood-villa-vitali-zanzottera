package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

@JsonTypeName("no_constraint")
public class NoConstraintEffect implements PlayableEffect{
    private final int cardValue;

    /**
     * This constructor creates a a no constraint effect
     * @param cardValue the points associated to the pattern
     */
    public NoConstraintEffect(@JsonProperty("value") int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        return cardValue;
    }

    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return new String[][]{{""}};
    }

    @Override
    public String getEffectDescription() {
        return "Points per pattern: " + this.cardValue + "\n" +
               "Required pattern: nothing";
    }

}
