package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.View.TUI.TUIView;

/**
 * Represents an effect of type "corner". It is used,
 * for example, for all cards that give points for every corner they hide
 */
@JsonTypeName("corner")
class CornerEffect implements PlayableEffect{

    /**
     * Card value of the card
     */
    private final int cardValue;

    /**
     * This constructor creates a corner effect
     * @param cardValue the points associated to the pattern
     */
    protected CornerEffect(@JsonProperty("value") int cardValue){
        this.cardValue = cardValue;
    }

    /**
     * Count points obtained by player from this effect
     * @param station the station where to count points
     * @return the number of points gained by player by this effect
     */
    @Override
    public int countPoints(Station station){
        int numCop = 0;
        for(Direction d : Direction.values()){
            try {
                if(station.getLastPlaced().isPresent() && station.getCardWithAnchor(station.getLastPlaced().get(), d).isPresent()){
                    numCop++;
                }
            }catch(Exception ignored){};
        }
        return numCop * this.cardValue;
    }

    /**
     * Getter for TUI-view visual description of the effect of the card
     * @param tuiView the {@link TUIView} that will display infos about effect
     * @return TUI-view visual description of the effect of the card
     */
    @Override
    public String[][] getEffectView(TUIView tuiView) {
        return new String[][]{{""}};
    }

    /**
     * Getter for string description of the card
     * @return the string description of the card
     */
    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "for every corner that this card covers";
    }

}