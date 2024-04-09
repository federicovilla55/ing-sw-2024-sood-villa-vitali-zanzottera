package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoConstraintEffect.class, name = "no_constraint"),
        @JsonSubTypes.Type(value = CornerEffect.class, name = "corner"),
        @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
})
/**
 * This interface represents the effect of a PlayableCard
 */
interface PlayableEffect extends Serializable{

    /**
     * This method returns a detailed description of card's effect
     */
    String getEffectDescription();

    /**
     * This method returns the points gained by the player placing this card CardOrientation.UP
     * @return points gained by this card effect
     */
    int countPoints(Station station);

}
