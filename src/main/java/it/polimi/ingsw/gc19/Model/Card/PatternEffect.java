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

@JsonTypeName("pattern")
public class PatternEffect implements GoalEffect{

    private final int cardValue;
    private final ArrayList<Tuple<Integer, Integer>> moves;
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

    public int getCardValue() {
        return cardValue;
    }

    public List<Tuple<Integer, Integer>> getMoves() {
        return List.copyOf(moves);
    }

    public List<Symbol> getRequiredSymbol() {
        return List.copyOf(requiredSymbol);
    }

    @Override
    public int countPoints(Station station){
        return this.cardValue * station.countPattern(moves, requiredSymbol);
    }

    @Override
    public String getEffectDescription(){
        return "This card gives " + this.cardValue + " points for every occurrence of the pattern in the player area.\n" +
               "The pattern must exactly respect color and position as illustrated by the card.\n" +
               "Each card can be used only once per card effect (no overlap).";
    }

    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return tuiView.goalEffectView(this);
    }

}
