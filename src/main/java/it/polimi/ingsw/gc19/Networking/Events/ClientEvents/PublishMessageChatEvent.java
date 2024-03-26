package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class PublishMessageChatEvent extends Event {
    String nickName;
    String gameName;
    String message;

    public PublishMessageChatEvent(String nickName, String gameName, String message)
    {
        this.nickName = nickName;
        this.gameName = gameName;
        this.message = message;
    }
}
