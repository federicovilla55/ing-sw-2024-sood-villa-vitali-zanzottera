package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Events.ClientEvents.*;
import it.polimi.ingsw.gc19.Networking.Events.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandle implements EventHandling, Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream out;

    private ObjectInputStream in;
    private String nickName;
    public ClientHandle(Socket clientSocket) throws IOException
    {
        this.clientSocket = clientSocket;
        this.nickName = null;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new  ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run()
    {
        if(nickName == null) {
            try {
                Event receivedEvent = (Event) in.readObject();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {

        }
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

    @Override
    public void handle(ReconnectEvent reconnectEvent) {

    }

}
