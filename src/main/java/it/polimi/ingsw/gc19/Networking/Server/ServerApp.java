package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ServerTcp;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.Math.abs;

public class ServerApp {

    public static List<ClientHandler> ActiveClient;
    public static MainController masterMainController;

    public static long MAXTIME = 10000;
    public static void main(String[] args) throws IOException {
        List<ClientHandler> ListClient = new ArrayList<ClientHandler>();;
        List<ClientHandler> ListNonActiveClient = new ArrayList<ClientHandler>();
        MainController masterMainController = MainController.getMainController();
        Registry registry = LocateRegistry.createRegistry(12122);
        MainServerRMI MainRmi = new MainServerRMI();
        VirtualMainServer stub = (VirtualMainServer) UnicastRemoteObject.exportObject(MainRmi, 0);
        registry.rebind(Settings.mainRMIServerName, stub);
        ServerTcp MainTcp = new ServerTcp(ListClient, masterMainController);
    }

}
