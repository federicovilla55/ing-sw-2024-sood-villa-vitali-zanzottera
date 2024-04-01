package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandle;

import java.util.List;

public class ServerSocket implements Runnable {

    private List<ClientHandle> ActiveList;
    final Controller MasterController;

    public ServerSocket(List<ClientHandle> ActiveList, Controller MasterController)
    {
        this.ActiveList = ActiveList;
        this.MasterController = MasterController;
    }
    public void run() //throws IOException
    {
        ServerSocket serverSocket = null;
       // serverSocket = new ServerSocket(12345);
        System.out.println("Server started.");
        while (true) {
            //Socket clientSocket = serverSocket.accept();
            //System.out.println("New client connected: " + clientSocket);
            //ClientHandleSocket Client = new ClientHandleSocket(clientSocket, MasterController);
            //ActiveList.add(Client);
            //Thread clientHandlerThread = new Thread(Client);
            //clientHandlerThread.start();
        }
    }
}
