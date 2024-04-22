package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPConnectionAcceptor extends Thread {

    private ServerSocket serverSocket;
    private final MainServerTCP mainServerTCP;

    public TCPConnectionAcceptor(MainServerTCP mainServerTCP, int port) {
        super();

        this.mainServerTCP = mainServerTCP;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            System.err.println("[EXCEPTION] IOException occurred when trying to build a server socket.");
        }
    }

    public void run() {
        Socket clientSocket;
        MessageToServerDispatcher messageToServerDispatcher;
        while (!this.isInterrupted()) {
            clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            }
            catch (SocketException ignored){ }
            catch (IOException ioException) {
                System.err.println("[EXCEPTION] IOException occurred while trying to accept connection from socket: " + ioException + " " + "Description: " + ioException.getMessage());
            }
            if (clientSocket != null) {
                messageToServerDispatcher = new MessageToServerDispatcher(clientSocket);
                messageToServerDispatcher.start();
                messageToServerDispatcher.attachObserver(this.mainServerTCP);
                mainServerTCP.registerSocket(clientSocket, messageToServerDispatcher);
            }
        }
    }

    public void interruptTCPConnectionAcceptor() {
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            System.err.println("[EXCEPTION] IOException occurred while trying to close server socket. Retrying...");
        }
        this.interrupt();
    }
}