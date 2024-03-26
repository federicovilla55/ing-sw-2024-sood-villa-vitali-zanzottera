package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Networking.Events.ClientEvents.*;

public interface EventHandling {
    public void handle(CreateGameEvent createGameEvent);

    public void handle(InsertCardEvent insertCardEvent);

    public void handle(JoinGameEvent joinGameEvent);

    public void handle(NewUserEvent newUserEvent);

    public void handle(PublishMessageChatEvent publishMessageChatEvent);

    public void handle(HeartBeatEvent heartBeatEvent);

    public void handle(ReconnectEvent reconnectEvent);

    public void handle(InitialCard initialCard);
}

