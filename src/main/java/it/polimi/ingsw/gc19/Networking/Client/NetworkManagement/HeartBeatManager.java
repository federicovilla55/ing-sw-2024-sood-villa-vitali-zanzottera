package it.polimi.ingsw.gc19.Networking.Client.NetworkManagement;

import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.NetworkManagementInterface;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeatManager{

    private final NetworkManagementInterface networkManagementInterface;
    private ScheduledExecutorService heartBeatSenderScheduler;
    private ScheduledExecutorService heartBeatChecker;
    private Long lastHeartBeatFromServer;
    private final Object lastHeartBeatLock;

    public HeartBeatManager(NetworkManagementInterface networkManagementInterface){
        this.networkManagementInterface = networkManagementInterface;

        this.heartBeatSenderScheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartBeatSenderScheduler.scheduleAtFixedRate(this::sendHeartBeat, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 2, TimeUnit.MILLISECONDS);
        this.heartBeatChecker = Executors.newSingleThreadScheduledExecutor();
        this.heartBeatChecker.scheduleAtFixedRate(this::runHeartBeatTesterForServer, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 2, TimeUnit.MILLISECONDS);

        this.lastHeartBeatFromServer = new Date().getTime();

        this.lastHeartBeatLock = new Object();
    }

    public void heartBeat(){
        synchronized (this.lastHeartBeatLock) {
            this.lastHeartBeatFromServer = new Date().getTime();
        }
    }

    private void runHeartBeatTesterForServer(){
        if(!Thread.currentThread().isInterrupted()) {
            synchronized (this.lastHeartBeatLock) {
                if (this.lastHeartBeatFromServer != null && new Date().getTime() - lastHeartBeatFromServer > 1000 * ClientSettings.MAX_TIME_BETWEEN_SERVER_HEARTBEAT_BEFORE_SIGNALING_NETWORK_PROBLEMS) {
                    stopHeartBeatManager();
                    networkManagementInterface.signalPossibleNetworkProblem();
                }
            }
        }
    }

    private void sendHeartBeat(){
        if(!Thread.currentThread().isInterrupted()) {
            try {
                this.networkManagementInterface.sendHeartBeat();
            } catch (RuntimeException runtimeException) {
                this.stopHeartBeatManager();
                this.networkManagementInterface.signalPossibleNetworkProblem();
            }
        }
    }

    public void startHeartBeatManager(){
        synchronized (this.lastHeartBeatLock) {
            if (this.lastHeartBeatFromServer == null) {
                this.lastHeartBeatFromServer = new Date().getTime();
            }
        }
        if(this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler = Executors.newSingleThreadScheduledExecutor();
            this.heartBeatSenderScheduler.scheduleAtFixedRate(this::sendHeartBeat, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 2, TimeUnit.MILLISECONDS);
        }
        if(this.heartBeatChecker.isShutdown()){
            this.heartBeatChecker = Executors.newSingleThreadScheduledExecutor();
            this.heartBeatChecker.scheduleAtFixedRate(this::runHeartBeatTesterForServer, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 2, TimeUnit.MILLISECONDS);
        }

    }

    public void stopHeartBeatManager(){
        if(!this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.shutdownNow();
        }
        if(!this.heartBeatChecker.isShutdown()){
            this.heartBeatChecker.shutdownNow();
        }
    }

}
