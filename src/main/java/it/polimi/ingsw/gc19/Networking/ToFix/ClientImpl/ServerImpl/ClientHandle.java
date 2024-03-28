package it.polimi.ingsw.gc19.Networking.ToFix.ClientImpl.ServerImpl;

import it.polimi.ingsw.gc19.Networking.Server.Message.NetworkMessage.HeartBeatEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ClientHandle /*implements EventHandling, Runnable*/ {
    /*private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nickName;

    private long GetLastTimeStep;
    public ClientHandle(Socket clientSocket) throws IOException
    {
        this.clientSocket = clientSocket;
        this.nickName = null;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new  ObjectInputStream(clientSocket.getInputStream());
        this.GetLastTimeStep = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        if(nickName == null) {
            try {
                Event receivedEvent = (Event) in.readObject();
                if(!(receivedEvent instanceof NewUserEvent) && !(receivedEvent instanceof ReconnectEvent)){
                    //error
                }
                else {
                    handleEvent(receivedEvent);
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                Event receivedEvent = (Event) in.readObject();
                handleEvent(receivedEvent);
            } catch (IOException | ClassNotFoundException e) {

            }

        }
    }

    public void handleEvent(Event event) {
        Class<? extends Event> eventType = event.getClass();
        try {
            Method handlerMethod = getClass().getMethod("handle", eventType);
            handlerMethod.invoke(this, event);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

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
        /*synchronized (this.GetLastTimeStep)
        {
            this.GetLastTimeStep = System.currentTimeMillis();
        }
    }

    @Override
    public void handle(ReconnectEvent reconnectEvent) {

    }*/

}
