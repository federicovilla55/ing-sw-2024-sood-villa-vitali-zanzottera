package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import it.polimi.ingsw.gc19.Enums.CardOrientation;

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
     * @return a {@link String} description of the effect
     */
    String getEffectDescription();

    /**
     * This method returns the points gained by the player placing this card {@link CardOrientation#UP}
     * @param station the {@link Station} on which count points
     * @return points gained by this card effect
     */
    int countPoints(Station station);

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the card
     */
    String[][] getEffectView(TUIView tuiView);
}