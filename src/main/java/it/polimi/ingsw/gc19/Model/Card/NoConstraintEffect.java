package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

/**
 * Represents a pattern with no constraints.
 * It is used, for example, for all cards that give
 * points only because they have been placed, and they
 * have no constraint.
 */
@JsonTypeName("no_constraint")
public class NoConstraintEffect implements PlayableEffect{

    /**
     * Value of the card
     */
    private final int cardValue;

    /**
     * This constructor creates a no constraint effect
     * @param cardValue the points associated to the pattern
     */
    public NoConstraintEffect(@JsonProperty("value") int cardValue){
        this.cardValue = cardValue;
    }

    /**
     * Count points obtained by player from this effect
     * @param station the station where to count points
     * @return the number of points gained by player by this effect
     */
    @Override
    public int countPoints(Station station){
        return cardValue;
    }

    /**
     * Getter for TUI-view visual description of the effect of the effect
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the effect
     */
    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return new String[][]{{""}};
    }

    /**
     * Getter for string description of the effect
     * @return the string description of the effect
     */
    @Override
    public String getEffectDescription() {
        return "Points per pattern: " + this.cardValue + "\n" +
               "Required pattern: nothing";
    }

}