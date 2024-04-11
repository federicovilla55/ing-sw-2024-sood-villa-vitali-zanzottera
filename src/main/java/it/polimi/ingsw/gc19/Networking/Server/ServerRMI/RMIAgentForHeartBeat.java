package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RMIAgentForHeartBeat implements VirtualHeartBeatServer{

    private enum State{
        ACTIVE, INACTIVE
    }

    private final MainServerRMI mainServerRMI;
    private VirtualClient virtualClientToControl;
    private final Object lockObj;
    private Long lastHeartBeatFromClient;
    private State state;

    public RMIAgentForHeartBeat(MainServerRMI mainServerRMI, VirtualClient virtualClient){
        this.mainServerRMI = mainServerRMI;
        this.lastHeartBeatFromClient = new Date().getTime();
        this.virtualClientToControl = virtualClient;
        this.lockObj = new Object();
        this.state = State.ACTIVE;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::heartBeatTester, 0, Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS * 3 / 2, TimeUnit.MILLISECONDS);
    }

    public void heartBeatTester(){
        synchronized (this.lockObj){
            if(this.lastHeartBeatFromClient - new Date().getTime() > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS){
                this.state = State.INACTIVE;
                this.mainServerRMI.notifyVirtualClientIsInactive(this.virtualClientToControl);
            }
        }
    }

    public void setStateToActive(){
        this.state = State.ACTIVE;
    }

    public void setVirtualClientToControl(VirtualClient virtualClient){
        this.virtualClientToControl = virtualClient;
    }

    @Override
    public void heartBeat() throws RemoteException {
        synchronized (this.lockObj){
            this.lastHeartBeatFromClient = new Date().getTime();
        }
    }

}
