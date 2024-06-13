package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.io.Serializable;

/**
 * This class represents a single card
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "card_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayableCard.class, name = "playable"),
        @JsonSubTypes.Type(value = GoalCard.class, name = "goal")
})
public abstract class Card implements Serializable {

    /**
     * This attribute uniquely identifies the card in game
     */
    private final String cardCode;

    /**
     * This constructor creates a card
     * @param cardCode the code that uniquely identifies the card in a game
     */
    @JsonCreator
    Card(@JsonProperty("code") String cardCode) {
        this.cardCode = cardCode;
    }

    /**
     * This method returns the code that uniquely identifies the card in a game
     * @return {@link #cardCode}
     */
    public String getCardCode() {
        return cardCode;
    }

    /**
     * This abstract method returns a detailed description of the card
     * @return a {@link String} description of the card
     */
    public abstract String getCardDescription();

    /**
     * This abstract method returns the points obtained by this card effect on a specific station
     * @param station the station where to count points
     * @return points gained by this card effect
     */
    public abstract int countPoints(Station station);

    /**
     * Overriding of {@link Object#equals(Object)} for {@link Card}.
     * Two card objects are equals if and only if their card codes are equal
     * @param obj the {@link Object} to compare
     * @return <code>true</code> if and ony if <code>obj</code> is a {@link Card}
     * and the card codes are equals
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(obj instanceof Card cardObj){
            return cardObj.cardCode.equals(this.cardCode);
        }
        return false;
    }

    /**
     * Builds a string description of the card
     * @return a string description of the card
     */
    @Override
    public String toString() {
        return this.getCardCode() +
                "\n" +
                this.getCardDescription();
    }
}