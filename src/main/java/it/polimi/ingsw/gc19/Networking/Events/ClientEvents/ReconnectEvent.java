package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class ReconnectEvent extends Event {
    String nickName;

    ReconnectEvent(String nickName) {
        this.nickName = nickName;
    }
}
