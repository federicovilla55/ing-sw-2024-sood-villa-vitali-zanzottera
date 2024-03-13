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
        @JsonSubTypes.Type(value = NoConstraintEffect.class, name = "no_constraint"),
        @JsonSubTypes.Type(value = CornerEffect.class, name = "corner"),
        @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
})
interface PlayableEffect{

    String getEffectDescription();
    int countPoints(Station station);

}
