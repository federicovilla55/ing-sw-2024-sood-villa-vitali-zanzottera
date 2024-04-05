package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Controller.MainServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerRmi.ServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ServerTcp;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.Math.abs;

public class ServerApp {

    public static List<ClientHandler> ActiveClient;
    public static MainServer masterMainServer;

    public static long MAXTIME = 10000;
    public static void main(String[] args) throws IOException {
        List<ClientHandler> ListClient = new ArrayList<ClientHandler>();;
        List<ClientHandler> ListNonActiveClient = new ArrayList<ClientHandler>();
        MainServer masterMainServer = MainServer.getController();
        ServerRMI MainRmi = new ServerRMI(masterMainServer);
        MainRmi.setController(masterMainServer);
        VirtualServer stub = (VirtualServer) UnicastRemoteObject.exportObject(MainRmi, 0);
        Registry registry = LocateRegistry.createRegistry(12122);
        registry.rebind("RMIServer", stub);
        ServerTcp MainTcp = new ServerTcp(ListClient, masterMainServer);
    }

}
