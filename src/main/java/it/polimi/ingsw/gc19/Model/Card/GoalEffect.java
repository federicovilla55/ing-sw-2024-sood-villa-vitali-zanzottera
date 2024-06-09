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
        property = "effect_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatternEffect.class, name = "pattern"),
        @JsonSubTypes.Type(value = SymbolEffect.class, name = "symbol")
})
interface GoalEffect extends Serializable{
    /**
     * This method returns the points gained by placing this card {@link CardOrientation#UP}.
     * @return points gained by this card effect
     */
    int countPoints(Station station);

    /**
     * This method returns a detailed description of the goal effect
     */
    String getEffectDescription();

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the card
     */
    String[][] getEffectView(TUIView tuiView);
}