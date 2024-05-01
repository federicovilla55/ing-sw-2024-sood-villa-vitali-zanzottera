package it.polimi.ingsw.gc19.Networking.Client;

public interface NetworkManagementInterface {
    void connect();
    void reconnect();
    void disconnect();
    void signalPossibleNetworkProblem();
    void sendHeartBeat();
}
