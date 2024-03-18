package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class PublishMessageChatEvent extends Event {
    String nickName;
    String gameName;
    String message;

    PublishMessageChatEvent(String nickName, String gameName, String message)
    {
        this.nickName = nickName;
        this.gameName = gameName;
        this.message = message;
    }
}
