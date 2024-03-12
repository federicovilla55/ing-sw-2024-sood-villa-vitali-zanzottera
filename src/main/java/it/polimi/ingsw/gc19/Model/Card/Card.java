package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Station.Station;

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

    private final String cardCode;

    protected Card(String cardCode){
        this.cardCode = cardCode;
    }

    public String getCardCode(){
        return cardCode;
    }

    public abstract String getCardDescription();

    public abstract int countPoints(Station station);

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Card cardObj){
            return cardObj.cardCode.equals(this.cardCode);
        }
        return false;
    }

}
