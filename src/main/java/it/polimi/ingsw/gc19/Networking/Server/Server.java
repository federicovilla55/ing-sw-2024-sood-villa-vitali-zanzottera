package it.polimi.ingsw.gc19.Networking.Server;
import it.polimi.ingsw.gc19.Controller.Controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static java.lang.Math.abs;

public class Server {

    static List<ClientHandle> ActiveClient;
    static List<ClientHandle> NonActiveClient;

    static Controller MasterController;

    private final static long MAXTIME = 1000;
    public static void main(String[] args) throws IOException{
        MasterController = new Controller();
        ActiveClient  = new ArrayList<ClientHandle>();
        NonActiveClient = new ArrayList<ClientHandle>();
        /*
        * Need to Find a way to tell java to wait a few seconds before
        * doing ControlActiveClient()
        * */
        Thread thread = new Thread(() -> {
            ControlActiveClient();
        });
        thread.start();
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(12345);
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);
            ClientHandle Client = new ClientHandle(clientSocket, MasterController);
            ActiveClient.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }

    public static void ControlActiveClient()
    {
        for(ClientHandle client : ActiveClient){
            if(client.getName() != null && abs(client.getGetLastTimeStep()-System.currentTimeMillis()) > MAXTIME) {
                MasterController.SetToNonActive(client.getName());
                NonActiveClient.add(client);
                ActiveClient.remove(client);
            }
        }

        for(ClientHandle client : NonActiveClient){
            if(abs(client.getGetLastTimeStep()-System.currentTimeMillis()) < MAXTIME) {
                MasterController.SetToNonActive(client.getName());
                MasterController.SetToActive(client.getName());
                ActiveClient.remove(client);
                NonActiveClient.add(client);
            }
        }
    }
}
