package it.polimi.ingsw.gc19.Networking.Client.NetworkManagement;

/**
 * This interface defines all methods, regarding network management, that
 * outside "network interface" can be called.
 */
public interface NetworkManagementInterface {
    /**
     * This method is used to connect client to server
     * @param nick the nickname of the client
     */
    void connect(String nick);

    /**
     * This method is used to reconnect to server
     */
    void reconnect();

    /**
     * This method is used to disconnect from server.
     */
    void disconnect();

    /**
     * This method is used to signal that a network problem maybe have occurred
     */
    void signalPossibleNetworkProblem();

    /**
     * This method is used to send heartbeat to server.
     */
    void sendHeartBeat();

    /**
     * This method is used to ask "network interface" to start sending heartbeats
     */
    void startSendingHeartbeat();

    /**
     * This method is used to ask "network-interface" to stop sending heartbeats.
     */
    void stopSendingHeartbeat();

    /**
     * This method is used to interrupt "network-interface".
     */
    void stopClient();
}