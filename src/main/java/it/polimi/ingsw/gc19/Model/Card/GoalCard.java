package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

/**
 * This class represents a single card
 */
public class GoalCard extends Card{
    /**
     * This attribute represents the effect that a goal
     * card has, that is activated when the game finishes
     * To see various effects, see classes that
     * implements GoalEffect
     */
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "effect_type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = PatternEffect.class, name = "pattern"),
            @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
    })
    private final GoalEffect goalEffect;

    /**
     * This constructor creates a goal card
     * @param cardCode the code that uniquely identifies a card in a game
     * @param goalEffect is the effect of the goal card
     */
    @JsonCreator
    public GoalCard(
            @JsonProperty("code") String cardCode,
            @JsonProperty("effect_type") GoalEffect goalEffect
    ){
        super(cardCode);
        this.goalEffect = goalEffect;
    }

    @Override
    public String getCardDescription(){return "Type: goal card " + this.goalEffect.getEffectDescription(); }

    @Override
    public int countPoints(Station station){
        return this.goalEffect.countPoints(station);
    }

}
