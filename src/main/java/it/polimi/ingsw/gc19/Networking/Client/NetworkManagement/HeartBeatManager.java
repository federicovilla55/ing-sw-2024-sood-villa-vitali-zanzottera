package it.polimi.ingsw.gc19.Networking.Client.NetworkManagement;

import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.NetworkManagementInterface;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeatManager{

    private final NetworkManagementInterface networkManagementInterface;
    private final ScheduledExecutorService heartBeatSenderScheduler;
    private final ScheduledExecutorService heartBeatChecker;
    private long lastHeartBeatFromServer;
    private final Object lastHeartBeatLock;

    public HeartBeatManager(NetworkManagementInterface networkManagementInterface){
        this.networkManagementInterface = networkManagementInterface;
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
                networkManagementInterface.signalPossibleNetworkProblem();
            }
        }
    }

    private void sendHeartBeat(){
        try{
            this.networkManagementInterface.sendHeartBeat();
        }
        catch (RuntimeException runtimeException){
            this.startHeartBeatManager();
            this.networkManagementInterface.signalPossibleNetworkProblem();
        }
    }

    public void startHeartBeatManager(){
        if(this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.scheduleAtFixedRate(this::sendHeartBeat, 0, 400, TimeUnit.MILLISECONDS);
        }
        if(this.heartBeatChecker.isShutdown()){
            this.heartBeatChecker.scheduleAtFixedRate(this::runHeartBeatTesterForServer, 0, 400, TimeUnit.MILLISECONDS);
        }
    }

    public void stopHeartBeatManager(){
        if(!this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.shutdownNow();
        }
        if(this.heartBeatChecker.isShutdown()){
            this.heartBeatChecker.shutdownNow();
        }
    }

}
