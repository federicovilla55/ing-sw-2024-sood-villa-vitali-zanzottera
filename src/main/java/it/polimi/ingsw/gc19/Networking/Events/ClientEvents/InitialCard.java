package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Networking.Events.Event;

public class InitialCard extends Event {
    public String nickName;
    public CardOrientation cardOrientation;

    public InitialCard(String nickName, CardOrientation cardOrientation){
        this.nickName = nickName;
        this.cardOrientation = cardOrientation;
    }
}
