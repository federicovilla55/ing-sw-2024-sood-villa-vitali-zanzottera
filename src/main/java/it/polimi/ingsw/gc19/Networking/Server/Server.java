package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ClientHandlerSocket;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This abstract class represents a generic server. In other words,
 * some "network interfaces" of the application
 */
public abstract class Server{

    /**
     * Connected {@link MainController}
     */
    protected final MainController mainController;

    protected Server(){
        this.mainController = MainController.getMainController();
    }

    /**
     * This method is used to compute, with MD5, the hash of a {@link ClientHandler} object
     * and nickname of its owner.
     * That hash, will be used as secret token for reconnection.
     * @param clientHandler the {@link ClientHandler} for which hash has to be computed
     * @param username the nickname of player owner
     * @return a {@link String} hash of the {@link ClientHandler} object and nickname.
     */
    protected String computeHashOfClientHandler(ClientHandler clientHandler, String username){
        String hashedMessage = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            hashedMessage = Arrays.toString(digest.digest((username + clientHandler.toString()).getBytes()));
        } catch (NoSuchAlgorithmException ignored) { };

        return hashedMessage;
    }

    /**
     * This method is used to kill (e.g. disconnect forcibly) all
     * connected clients.
     */
    protected abstract void killClientHandlers();

    /**
     * This method is used to check heart beat timing of connected clients.
     * If client do not send heart beat message for more than <code>Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS</code>
     * it deletes it from <code>lastHeartBeatOfClients</code> hashmap, it sets it to inactive
     * using {@link MainController#setPlayerInactive(String)}. It doesn't remove it from <code>connectedClients</code>
     * hashmap because client can reconnect, and thus it is necessary to keep its private token.
     */
    protected abstract void runHeartBeatTesterForClient();

    /**
     * This method reset server and {@link MainController}
     */
    protected abstract void resetServer();

}