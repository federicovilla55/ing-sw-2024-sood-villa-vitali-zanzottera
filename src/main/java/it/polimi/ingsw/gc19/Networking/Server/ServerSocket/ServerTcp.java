package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerTcp implements Runnable {

    private  List<ClientHandler> ActiveList;
    final MainController masterMainController;

    private ServerSocket serverSocket;

    public ServerTcp(List<ClientHandler> ActiveList, MainController masterMainController) throws IOException
    {
        this.ActiveList = ActiveList;
        this.masterMainController = masterMainController;
        this.serverSocket = new ServerSocket(1234);
    }
    public void run()
    {
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("New client connected: " + clientSocket);
            ClientHandlerSocket Client = null;
            try {
                Client = new ClientHandlerSocket(clientSocket, masterMainController);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ActiveList.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }
}
