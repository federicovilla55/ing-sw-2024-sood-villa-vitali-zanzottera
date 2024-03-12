package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "effect_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatternEffect.class, name = "pattern"),
        @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
})
interface GoalEffect{

    int countPoints(Station station);
    String getEffectDescription();

}
