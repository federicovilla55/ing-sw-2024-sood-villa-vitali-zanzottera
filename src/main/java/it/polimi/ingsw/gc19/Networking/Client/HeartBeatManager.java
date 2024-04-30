package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class HeartBeatManager{

    private final ClientInterface clientInterface;
    private final ScheduledExecutorService heartBeatSenderScheduler;
    private final ScheduledExecutorService heartBeatChecker;
    private long lastHeartBeatFromServer;
    private final Object lastHeartBeatLock;

    public HeartBeatManager(ClientInterface clientInterface){
        this.clientInterface = clientInterface;
        this.heartBeatSenderScheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartBeatChecker = Executors.newSingleThreadScheduledExecutor();
        this.lastHeartBeatLock = new Object();
    }

    public void heartBeat(){
        synchronized (this.lastHeartBeatLock) {
            this.lastHeartBeatFromServer = new Date().getTime();
        }
    }

    private void runHeartBeatTesterForServer(){
        synchronized (this.lastHeartBeatLock){
            if(new Date().getTime() - lastHeartBeatFromServer > 30 * 1000){
                clientInterface.signalDisconnection();
            }
        }
    }

    public void startHeartBeatManager(){
        if(this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.scheduleAtFixedRate(clientInterface::heartBeat, 0, 400, TimeUnit.MILLISECONDS);
        }
    }

    public void stopHeartBeatManager(){
        if(!this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.shutdownNow();
        }
    }


}
