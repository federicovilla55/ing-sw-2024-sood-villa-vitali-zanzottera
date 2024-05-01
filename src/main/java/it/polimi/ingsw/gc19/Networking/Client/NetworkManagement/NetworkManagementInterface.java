package it.polimi.ingsw.gc19.Networking.Client.NetworkManagement;

public interface NetworkManagementInterface {
    void connect(String nick);
    void reconnect();
    void disconnect();
    void signalPossibleNetworkProblem();
    void sendHeartBeat();
    void startSendingHeartbeat();
    void stopSendingHeartbeat();
}
