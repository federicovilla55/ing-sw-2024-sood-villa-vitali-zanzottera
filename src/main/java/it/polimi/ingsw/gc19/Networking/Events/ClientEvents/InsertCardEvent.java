package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Events.Event;

public class InsertCardEvent extends Event {

    public String gameName;
    public String nickname;
    public String anchorCard;
    public Direction direction;
    public String cardName;
    public InsertCardEvent(String nickname, String gameName, String cardName ,String anchorCard, Direction direction){
        this.nickname = nickname;
        this.gameName = gameName;
        this.direction = direction;
        this.anchorCard = anchorCard;
        this.cardName = cardName;
    }
}
