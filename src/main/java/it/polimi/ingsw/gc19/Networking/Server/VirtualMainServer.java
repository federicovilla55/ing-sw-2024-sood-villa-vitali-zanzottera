package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.VirtualClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents the remote interface of the {@link it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI}.
 * RMI clients can call remote method specified in this interface.
 */
public interface VirtualMainServer extends Remote {

    /**
     * This method is used when a new client wants to connect to server.
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to connect
     * @param nickName is the name of the client
     * @throws RemoteException if something goes wrong executing this method
     */
    void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException;

    /**
     * This method is used by client to create a new game
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to create a new game
     * @param gameName is the name of the game to build
     * @param nickname is the nickname of the player
     * @param numPlayer is the number of the player in game
     * @return {@link VirtualGameServer} representing the game server (note that for clients connected to
     * same game, different {@link VirtualGameServer} will be returned). Returns <code>null</code> if action cannot be performed.
     * @throws RemoteException if something goes wrong executing this method
     */
    VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickname, int numPlayer) throws RemoteException;

    /**
     * This method is used by client to create a new game
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to create a new game
     * @param gameName is the name of the game to build
     * @param nickname is the nickname of the player
     * @param numPlayer is the number of the player in game
     * @param randomSeed is the random seed on which build the game
     * @return {@link VirtualGameServer} representing the game server (note that for clients connected to
     * same game, different {@link VirtualGameServer} will be returned). Returns <code>null</code> if action cannot be performed.
     * @throws RemoteException if something goes wrong executing this method
     */
    VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickname, int numPlayer, long randomSeed) throws RemoteException;

    /**
     * This method is used by client to join game with the specified name
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to join game
     * @param gameName is the name of the game to enter
     * @param nickname is the nickname of the requesting player
     * @return {@link VirtualGameServer} representing the game server (note that for clients connected to
     * same game, different {@link VirtualGameServer} will be returned). Returns <code>null</code> if action cannot be performed.
     * @throws RemoteException if something goes wrong while executing this method.
     */
    VirtualGameServer joinGame(VirtualClient clientRMI, String gameName, String nickname) throws RemoteException;

    /**
     * This method is used by client to join first available game
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to join a game
     * @param nickname is the nickname of the requesting player
     * @return {@link VirtualGameServer} representing the game server (note that for clients connected to
     * same game, different {@link VirtualGameServer} will be returned). Returns <code>null</code> if action cannot be performed
     * or there are no available games.
     * @throws RemoteException if something goes wrong while performing the requested action.
     */
    VirtualGameServer joinFirstAvailableGame(VirtualClient clientRMI, String nickname) throws RemoteException;

    /**
     * This method is used by client to reconnect to server (after network disconnection,not
     * explicit disconnection).
     * @param clientRMI is the {@link VirtualClient} of the RMI client who wants to reconnect
     * @param nickname is the nickname of the requesting client
     * @param token is the private token of the client
     * @return {@link VirtualGameServer} representing the game server (note that for clients connected to
     * same game, different {@link VirtualGameServer} will be returned). Returns <code>null</code> if
     * reconnection cannot be performed (for example client is not registered in server) or client
     * is not registered to any game.
     * @throws RemoteException if something goes wrong while performing the requested action.
     */
    VirtualGameServer reconnect(VirtualClient clientRMI, String nickname, String token) throws RemoteException;

    /**
     * This method is used by clients who need to explicitly disconnect themselves from server.
     * @param clientRMI is {@link VirtualClient} of the requesting client
     * @param nickname is the nickname of the requesting client
     * @throws RemoteException if something goes wrong while performing the requested action
     */
    void disconnect(VirtualClient clientRMI, String nickname) throws RemoteException;

    /**
     * This method is used by client to notify {@link it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI}
     * that they are still alive.
     * @param clientRMI is the {@link VirtualClient} of the sender client
     * @throws RemoteException if something goes wrong while performing the requested action.
     */
    void heartBeat(VirtualClient clientRMI) throws RemoteException;

}


