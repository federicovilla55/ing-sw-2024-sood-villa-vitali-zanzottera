package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "effect_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatternEffect.class, name = "pattern"),
        @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
})
interface GoalEffect extends Serializable{
    /**
     * This method returns the points gained by placing this card UP.
     * @return points gained by this card effect
     */
    int countPoints(Station station);

    /**
     * This method returns a detailed description of the goal effect
     */
    String getEffectDescription();

}
