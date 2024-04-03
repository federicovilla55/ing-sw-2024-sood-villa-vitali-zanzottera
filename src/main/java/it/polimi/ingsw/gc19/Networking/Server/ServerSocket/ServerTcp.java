package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerTcp implements Runnable {

    private  List<HandleClient> ActiveList;
    final Controller MasterController;

    private ServerSocket serverSocket;

    public ServerTcp(List<HandleClient> ActiveList, Controller MasterController) throws IOException
    {
        this.ActiveList = ActiveList;
        this.MasterController = MasterController;
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
            ClientHandleSocket Client = null;
            try {
                Client = new ClientHandleSocket(clientSocket, MasterController);
            } catch (IOException e) {}
            ActiveList.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }
}
