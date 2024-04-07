package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface VirtualGameServer extends Remote {
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) throws RemoteException;
    public void heartBeat() throws RemoteException;
    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend) throws RemoteException;
    public void placeInitialCard(CardOrientation cardOrientation) throws RemoteException;
    public void pickCardFromTable(PlayableCardType type, int position) throws RemoteException;
    public void pickCardFromDeck(PlayableCardType type) throws RemoteException;
}
