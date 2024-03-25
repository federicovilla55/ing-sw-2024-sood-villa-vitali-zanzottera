package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Events.ClientEvents.*;
import it.polimi.ingsw.gc19.Networking.Events.Event;
import it.polimi.ingsw.gc19.Networking.Events.ServerEvents.NotSuccessEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ClientHandle implements EventHandling, Runnable {
    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private String nickName;

    private Controller MasterController;
    private long getLastTimeStep;
    private final Object getLastTimeStepLock;

    public ClientHandle(Socket clientSocket, Controller MasterController) throws IOException
    {
        this.clientSocket = clientSocket;
        this.nickName = null;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new  ObjectInputStream(clientSocket.getInputStream());
        this.getLastTimeStep = System.currentTimeMillis();
        this.getLastTimeStepLock = new Object();
        this.MasterController = MasterController;
    }

    public long getGetLastTimeStep()
    {
        synchronized (getLastTimeStepLock){
            return this.getLastTimeStep;
        }
    }

    public String getName()
    {
        return this.nickName;
    }

    @Override
    public void run()
    {

        while(nickName == null)
        {
            try {
                Event receivedEvent = (Event) in.readObject();
                if(!(receivedEvent instanceof NewUserEvent) && !(receivedEvent instanceof ReconnectEvent)){
                    //error
                }
                else {handleEvent(receivedEvent);}

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Event receivedEvent = (Event) in.readObject();
            handleEvent(receivedEvent);
        } catch (IOException | ClassNotFoundException e){}
    }

    public void handleEvent(Event event) {
        Class<? extends Event> eventType = event.getClass();
        try {
            Method handlerMethod = getClass().getMethod("handle", eventType);
            handlerMethod.invoke(this, event);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {}
    }
    @Override
    public void handle(CreateGameEvent createGameEvent) {
        try{
            MasterController.createGame(this.nickName, createGameEvent.gameName, createGameEvent.numPlayer);}
        catch (IOException e) {
            Event sendError= new NotSuccessEvent("Game with name already exists!");
            synchronized(out) {
                try {out.writeObject(sendError);} catch (IOException ex){ }
            }
        }
        synchronized (this.getLastTimeStepLock)
        {
            this.getLastTimeStep = System.currentTimeMillis();
        }

    }

    @Override
    public void handle(InsertCardEvent insertCardEvent) {
        MasterController.makeMove(insertCardEvent.nickname, insertCardEvent.cardName, insertCardEvent.anchorCard,
                insertCardEvent.direction);

    }

    @Override
    public void handle(JoinGameEvent joinGameEvent) {

    }

    @Override
    public void handle(NewUserEvent newUserEvent) {
        boolean check = MasterController.NewClient(newUserEvent.nickName);
        if(!check){
            Event sendError= new NotSuccessEvent("Username already Exists!");
            synchronized(out) {
                try {out.writeObject(sendError);} catch (IOException ex){ }
            }
        }
        else this.nickName = newUserEvent.nickName;
    }

    @Override
    public void handle(PublishMessageChatEvent publishMessageChatEvent) {

    }

    @Override
    public void handle(HeartBeatEvent heartBeatEvent) {
        synchronized (this.getLastTimeStepLock)
        {
            this.getLastTimeStep = System.currentTimeMillis();
        }
    }

    @Override
    public void handle(ReconnectEvent reconnectEvent) {

    }

    @Override
    public void handle(InitialCard initialCard) {
        MasterController.setInitialCard(initialCard.nickName, initialCard.cardOrientation);
    }

}
