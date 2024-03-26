package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class HeartBeatEvent extends Event {
    public String nickName;
    public HeartBeatEvent(String nickName)
    {
         this.nickName = nickName;
    }
}
