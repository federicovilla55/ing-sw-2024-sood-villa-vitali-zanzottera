package it.polimi.ingsw.gc19.Networking.Client.NetworkManagement;

import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat.ServerHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to manage both client-to-server and
 * server-to-client heartbeats. It detects possible network problems
 * and signal them to the "network-interface" it is bound.
 */
public class HeartBeatManager{

    private final NetworkManagementInterface networkManagementInterface;
    private ScheduledExecutorService heartBeatSenderScheduler;
    private ScheduledExecutorService heartBeatChecker;
    private Long lastHeartBeatFromServer;
    private final Object lastHeartBeatLock;

    public HeartBeatManager(NetworkManagementInterface networkManagementInterface){
        this.networkManagementInterface = networkManagementInterface;

        this.heartBeatSenderScheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartBeatSenderScheduler.scheduleAtFixedRate(this::sendHeartBeat, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);
        this.heartBeatChecker = Executors.newSingleThreadScheduledExecutor();
        this.heartBeatChecker.scheduleAtFixedRate(this::runHeartBeatTesterForServer, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);

        this.lastHeartBeatFromServer = new Date().getTime();

        this.lastHeartBeatLock = new Object();
    }

    /**
     * This method is used by "network-interface" to signal to {@link HeartBeatManager}
     * that a new {@link ServerHeartBeatMessage} has arrived.
     */
    public void heartBeat(){
        synchronized (this.lastHeartBeatLock) {
            this.lastHeartBeatFromServer = new Date().getTime();
        }
    }

    /**
     * This method tests if {@link ServerHeartBeatMessage} have arrived correctly.
     * In other words, it tests if in the last {@link ClientSettings#WAIT_BETWEEN_RECONNECTION_TRY_IN_CASE_OF_EXPLICIT_NETWORK_ERROR}
     * have arrived at least one {@link ServerHeartBeatMessage}
     */
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

    /**
     * This method is used to send heartbeats to server periodically.
     */
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

    /**
     * This method is used to start {@link HeartBeatManager}. It starts
     * both {@link HeartBeatManager#heartBeatSenderScheduler} and {@link HeartBeatManager#heartBeatChecker}
     * {@link ScheduledExecutorService}.
     */
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

    /**
     * This method is used to stop {@link HeartBeatManager}. It shuts down
     * both {@link HeartBeatManager#heartBeatSenderScheduler} and {@link HeartBeatManager#heartBeatChecker}
     * {@link ScheduledExecutorService}.
     */
    public void stopHeartBeatManager(){
        if(!this.heartBeatSenderScheduler.isShutdown()){
            this.heartBeatSenderScheduler.shutdownNow();
        }
        if(!this.heartBeatChecker.isShutdown()){
            this.heartBeatChecker.shutdownNow();
        }
    }

}