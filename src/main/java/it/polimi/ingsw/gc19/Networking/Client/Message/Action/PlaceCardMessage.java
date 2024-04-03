package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.Direction;

public class PlaceCardMessage implements ActionMessage{

    private final String playerNickname;
    private final String cardToPlaceCode;
    private final String anchorCode;
    private final Direction direction;

    public PlaceCardMessage(String playerNickname, String cardToPlaceCode, String anchorCode, Direction direction){
        this.playerNickname = playerNickname;
        this.cardToPlaceCode = cardToPlaceCode;
        this.anchorCode = anchorCode;
        this.direction = direction;
    }

    public String getPlayerNickname() {
        return this.playerNickname;
    }

    public String getCardToPlaceCode() {
        return this.cardToPlaceCode;
    }

    public String getAnchorCode() {
        return this.anchorCode;
    }

    public Direction getDirection() {
        return this.direction;
    }

}
