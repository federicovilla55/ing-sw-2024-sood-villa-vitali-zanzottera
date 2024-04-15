package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MainServerTCP;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.TCPConnectionAcceptor;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServerApp {

    public static List<ClientHandler> ActiveClient;
    public static MainController masterMainController;

    public static VirtualMainServer instance;

    private static VirtualMainServer getVirtualMainServer() {
        if (instance==null)
            instance = new MainServerRMI();
        return instance;
    }

    private static Registry registry;

    private static VirtualMainServer stub;
    private static TCPConnectionAcceptor MainTcp;

    public static void main(String[] args) throws IOException {
        //List<ClientHandler> ListClient = new ArrayList<ClientHandler>();;
        //List<ClientHandler> ListNonActiveClient = new ArrayList<ClientHandler>();
        //MainController masterMainController = MainController.getMainController();
        startRMI();
        startTCP();
    }

    public static void startRMI() throws RemoteException {
        registry = LocateRegistry.createRegistry(1099);
        stub = (VirtualMainServer) UnicastRemoteObject.exportObject(ServerApp.getVirtualMainServer(),0);
        registry.rebind(Settings.mainRMIServerName, stub);
    }

    public static void startTCP(){
        MainTcp = new TCPConnectionAcceptor(new MainServerTCP());
        MainTcp.start();
    }

    public static void unexportRegistry() {
        try {
            UnicastRemoteObject.unexportObject(ServerApp.getVirtualMainServer(), true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stopTCP(){
        MainTcp.interrupt();
    }
}
