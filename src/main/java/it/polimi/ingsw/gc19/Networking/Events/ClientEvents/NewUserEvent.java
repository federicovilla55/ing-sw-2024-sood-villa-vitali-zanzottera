package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class NewUserEvent extends Event {
    public String nickName;
    public NewUserEvent(String nickName)
    {
        this.nickName = nickName;
    }
}
