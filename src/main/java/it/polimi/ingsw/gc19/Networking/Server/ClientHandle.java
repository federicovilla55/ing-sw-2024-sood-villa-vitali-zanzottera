package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Events.ClientEvents.*;

import java.net.Socket;

public class ClientHandle implements EventHandling, Runnable {
    private final Socket clientSocket;
    public ClientHandle(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {

    }


    @Override
    public void handle(CreateGameEvent createGameEvent) {

    }

    @Override
    public void handle(InsertCardEvent insertCardEvent) {

    }

    @Override
    public void handle(JoinGameEvent joinGameEvent) {

    }

    @Override
    public void handle(NewUserEvent newUserEvent) {

    }

    @Override
    public void handle(PublishMessageChatEvent publishMessageChatEvent) {

    }

    @Override
    public void handle(HeartBeatEvent heartBeatEvent) {

    }

}
