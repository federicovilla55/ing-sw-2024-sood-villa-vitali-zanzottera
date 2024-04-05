package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.MainServer;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerTcp implements Runnable {

    private  List<ClientHandler> ActiveList;
    final MainServer masterMainServer;

    private ServerSocket serverSocket;

    public ServerTcp(List<ClientHandler> ActiveList, MainServer masterMainServer) throws IOException
    {
        this.ActiveList = ActiveList;
        this.masterMainServer = masterMainServer;
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
            Client = new ClientHandleSocket(clientSocket, masterMainServer);
            ActiveList.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }
}
