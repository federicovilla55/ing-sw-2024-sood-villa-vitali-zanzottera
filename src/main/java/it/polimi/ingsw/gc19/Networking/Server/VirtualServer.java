package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface VirtualServer extends Remote {
    public void newConnection(VirtualClient clientRmi, String nickName) throws RemoteException;
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException;
    public void joinGame(VirtualClient clientRMI, String GameName) throws RemoteException;
    public void placeCard(VirtualClient clientRMI, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException;
    public void heartBeat(VirtualClient clientRMI) throws RemoteException;
    public void reconnect(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException;
    public void sendChatMessage(VirtualClient clientRMI, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException;
    public void placeInitialCard(VirtualClient clientRMI, CardOrientation cardOrientation) throws RemoteException;
    public void pickCardFromTable(VirtualClient clientRMI, PlayableCardType type, int position) throws RemoteException;
    public void pickCardFromDeck(VirtualClient clientRMI, PlayableCardType type) throws RemoteException;
    void disconnect(VirtualClient clientRMI, String nickname) throws RemoteException;
}
