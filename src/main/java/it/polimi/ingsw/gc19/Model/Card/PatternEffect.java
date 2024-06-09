package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a constrained effect of type "pattern", such as
 * "L-pattern" or "Diagonal-pattern"
 */
@JsonTypeName("pattern")
public class PatternEffect implements GoalEffect{

    /**
     * Value of the card
     */
    private final int cardValue;

    /**
     * Required moves on the grid to build the pattern.
     * Each move must be intended in the form of <code>{-1, 1} X {-1, 1}</code>
     */
    private final ArrayList<Tuple<Integer, Integer>> moves;

    /**
     * Required {@link Symbol} for the pattern. First
     * symbol in the <code>ArrayList</code> corresponds to first
     * move in {@link #moves} and so on.
     */
    private final ArrayList<Symbol> requiredSymbol;

    /**
     * This constructor creates a corner effect
     * @param cardValue the points associated to the pattern
     * @param moves moves in card schema to realize the patter
     * @param requiredSymbol required card seeds for the pattern
     */
    @JsonCreator
    protected PatternEffect(@JsonProperty("value") int cardValue,
                            @JsonProperty("moves") ArrayList<Tuple<Integer, Integer>> moves,
                            @JsonProperty("required") ArrayList<Symbol> requiredSymbol){
        this.cardValue = cardValue;
        this.moves = moves;
        this.requiredSymbol = requiredSymbol;
    }

    /**
     * Getter for card value
     * @return the card value of the card
     */
    public int getCardValue() {
        return cardValue;
    }

    /**
     * Getter for required moves to build pattern
     * @return the required moves to build pattern
     */
    public List<Tuple<Integer, Integer>> getMoves() {
        return List.copyOf(moves);
    }

    /**
     * Getter for required {@link Symbol} to build pattern
     * @return the required {@link Symbol} to build pattern
     */
    public List<Symbol> getRequiredSymbol() {
        return List.copyOf(requiredSymbol);
    }

    /**
     * Count points obtained by player from this effect
     * @param station the station where to count points
     * @return the number of points gained by player by this effect
     */
    @Override
    public int countPoints(Station station){
        return this.cardValue * station.countPattern(moves, requiredSymbol);
    }

    /**
     * Getter for string description of the effect
     * @return the string description of the effect
     */
    @Override
    public String getEffectDescription(){
        return "This card gives " + this.cardValue + " points for every occurrence of the pattern in the player area.\n" +
               "The pattern must exactly respect color and position as illustrated by the card.\n" +
               "Each card can be used only once per card effect (no overlap).";
    }

    /**
     * Getter for TUI-view visual description of the effect of the effect
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the effect
     */
    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return tuiView.goalEffectView(this);
    }

}