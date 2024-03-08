package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

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
public abstract class Card{

    /**
     * This attribute uniquely identifies the card in game
     */
    private final String cardCode;

    /**
     * This constructor creates a card
     * @param cardCode the code that uniquely identifies the card in a game
     */
   protected Card(String cardCode) {
        this.cardCode = cardCode;
    }

    /**
     * This method returns the code that uniquely identifies the card in a game
     * @return this.cardCode
     */
    public String getCardCode() {
        return cardCode;
    }

    /**
     * This abstract method returns a detailed description of the card
     */
    public abstract String getCardDescription();

    /**
     * This abstract method returns the points obtained by this card effect on a specific station
     * @param station the station where to count points
     * @return points gained by this card effect
     */
    public abstract int countPoints(Station station);

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Card cardObj){
            return cardObj.cardCode.equals(this.cardCode);
        }
        return false;
    }

}
