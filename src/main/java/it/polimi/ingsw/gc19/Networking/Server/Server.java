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

    protected final MainController mainController;

    protected Server(){
        this.mainController = MainController.getMainController();
    }

    protected String computeHashOfClientHandler(ClientHandler clientHandler, String username){
        String hashedMessage = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            hashedMessage = Arrays.toString(digest.digest((username + clientHandler.toString()).getBytes()));
        } catch (NoSuchAlgorithmException ignored) { };

        return hashedMessage;
    }

}
