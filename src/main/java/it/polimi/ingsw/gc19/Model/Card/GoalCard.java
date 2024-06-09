package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.*;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

/**
 * This class represents a single card
 */
@JsonTypeName("goal")
public class GoalCard extends Card {

    /**
     * This attribute represents the effect that a goal
     * card has, that is activated when the game finishes
     * To see various effects, see classes that
     * implements {@link GoalEffect}
     */
    private final GoalEffect goalEffect;

    /**
     * This constructor creates a goal card
     * @param cardCode the code that uniquely identifies a card in a game
     * @param goalEffect is the effect of the goal card
     */
    @JsonCreator
    public GoalCard(
            @JsonProperty("code") String cardCode,
            @JsonProperty("goal_effect") GoalEffect goalEffect
    ){
        super(cardCode);
        this.goalEffect = goalEffect;
    }

    /**
     * Getter for string description of the {@link GoalCard}
     * @return a {@link String} description of the {@link GoalCard}
     */
    @Override
    public String getCardDescription(){return "Goal card " + this.getCardCode() + ":\n" + this.goalEffect.getEffectDescription(); }

    /**
     * Count points obtained by player from this card
     * @param station the station where to count points
     * @return the number of points gained by player by this card
     */
    @Override
    public int countPoints(Station station){
        return this.goalEffect.countPoints(station);
    }

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about card
     * @return TUI-view visual description of the effect of the card
     */
    public String[][] getEffectView(TUIView tuiView) {
        return this.goalEffect.getEffectView(tuiView);
    }
}