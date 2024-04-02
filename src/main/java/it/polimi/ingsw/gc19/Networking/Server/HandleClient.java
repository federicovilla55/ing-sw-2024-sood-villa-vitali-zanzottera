package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayDeque;
import java.util.Queue;

public class HandleClient implements Observer<MessageToClient>{
    protected String username;
    protected long getLastTimeStep;
    protected final Queue<MessageToClient> messageQueue;
    private final Object getLastTimeStepLock;

    public HandleClient() {
        this.getLastTimeStepLock = new Object();
        this.messageQueue = new ArrayDeque<>();
    }

    public void SendMessageToClient() {
    }

    public void UpdateHeartBeat() {
        this.getLastTimeStep = System.currentTimeMillis();
    }
    public String getName() {
        return this.username;
    }

    public long getGetLastTimeStep()
    {
        synchronized (getLastTimeStepLock){
            return this.getLastTimeStep;
        }
    }
    @Override
    public void update(MessageToClient message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
        }
    }
}
