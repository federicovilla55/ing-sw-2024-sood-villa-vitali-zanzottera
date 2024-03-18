package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class JoinGameEvent extends Event {
    String gameName;
    String nickName;

    JoinGameEvent(String gameName, String nickName)
    {
        this.gameName = gameName;
        this.nickName = nickName;
    }
}
