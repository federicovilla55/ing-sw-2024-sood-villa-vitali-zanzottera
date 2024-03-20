package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Model.Tuple;

import java.util.ArrayList;

@JsonTypeName("pattern")
class PatternEffect implements GoalEffect{

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

    @Override
    public int countPoints(Station station){
        return this.cardValue * station.countPattern(moves, requiredSymbol);
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "\n" +
               "Pattern moves required starting from a card: " + this.moves.toString() + "\n" +
               "Patter resource required: " + this.requiredSymbol.toString();
    }

}
