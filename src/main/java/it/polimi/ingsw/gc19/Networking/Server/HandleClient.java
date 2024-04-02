package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;
import java.util.Queue;

public class HandleClient implements Observer{
    protected String Username;
    protected long GetLastTimeStep;
    protected Queue<MessageToClient> MessageQueue;
    private final Object getLastTimeStepLock;

    public HandleClient() {
        this.getLastTimeStepLock = new Object();
    }

    public void SendMessageToClient() {
    }

    public void UpdateHeartBeat() {
        this.GetLastTimeStep = System.currentTimeMillis();
    }
    public String getName() {
        return this.Username;
    }

    public long getGetLastTimeStep()
    {
        synchronized (getLastTimeStepLock){
            return this.GetLastTimeStep;
        }
    }
    @Override
    public void update(MessageToClient message) {
        synchronized (MessageQueue) {
            MessageQueue.add(message);
        }
    }
}
