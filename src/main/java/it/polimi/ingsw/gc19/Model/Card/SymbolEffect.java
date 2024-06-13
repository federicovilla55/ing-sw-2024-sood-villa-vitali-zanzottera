package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an effect of type "symbol". It is used,
 * for example, for all cards that give points depending on groups
 * of visible symbols
 */
@JsonTypeName("symbol")
public class SymbolEffect implements GoalEffect, PlayableEffect{

    /**
     * Required symbols for the effect
     */
    private final HashMap<Symbol, Integer> requiredSymbol;

    /**
     * Value of the card
     */
    private final int cardValue;

    /**
     * This constructor creates a symbol effect
     * @param requiredSymbol the required symbols to have effect
     * @param cardValue the points associated to the pattern
     */
    @JsonCreator
    SymbolEffect(@JsonProperty("required") HashMap<Symbol, Integer> requiredSymbol,
                 @JsonProperty("value") int cardValue){
        this.requiredSymbol = requiredSymbol;
        this.cardValue = cardValue;
    }

    /**
     * Getter for required {@link Symbol}.
     * @return the <code>Map&lt;Symbol, Integer&gt;</code> containing the
     * number of {@link Symbol} necessary to count points.
     */
    public Map<Symbol, Integer> getRequiredSymbol() {
        return Map.copyOf(requiredSymbol);
    }

    /**
     * Getter for string description of the effect
     * @return the string description of the effect
     */
    @Override
    public String getEffectDescription(){
        return "This card gives " + this.cardValue + " points for every group of indicated symbols.\n" +
               "The symbols are the visible symbols in the player area.";
    }

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the card
     */
    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return tuiView.goalEffectView(this);
    }

    /**
     * Count points obtained by player from this effect
     * @param station the {@link Station} where to count points
     * @return the number of points gained by player by this effect
     */
    @Override
    public int countPoints(Station station){
        return this.cardValue * this.requiredSymbol
                                    .entrySet()
                                    .stream()
                                    .filter(e -> e.getValue() != 0)
                                    .mapToInt(s -> station.getVisibleSymbolsInStation().get(s.getKey()) / s.getValue())
                                    .min()
                                    .orElse(0);

    }

}