package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MainServerTCP;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.TCPConnectionAcceptor;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerApp {

    private static Registry registry;
    private static TCPConnectionAcceptor MainTcp;
    private static MainServerTCP mainServerTCP;
    private static MainServerRMI mainServerRMI;

    public static void main(String[] args) throws IOException {
        //List<ClientHandler> ListClient = new ArrayList<ClientHandler>();;
        //List<ClientHandler> ListNonActiveClient = new ArrayList<ClientHandler>();
        //MainController masterMainController = MainController.getMainController();
        startRMI();
        startTCP();
    }

    public static void startRMI() throws RemoteException {
        mainServerRMI = MainServerRMI.getInstance();
        registry = LocateRegistry.createRegistry(1099);
        VirtualMainServer stub = (VirtualMainServer) UnicastRemoteObject.exportObject(mainServerRMI, 0);
        registry.rebind(Settings.mainRMIServerName, stub);
    }

    public static void startTCP(){
        mainServerTCP = MainServerTCP.getInstance();
        MainTcp = new TCPConnectionAcceptor(mainServerTCP);
        MainTcp.start();
    }

    public static void unexportRegistry() {
        try {
            UnicastRemoteObject.unexportObject(mainServerRMI, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stopRMI(){
        mainServerRMI.killClientHandlers();
        mainServerRMI.resetServer();
    }

    public static void stopTCP(){
        mainServerTCP.killClientHandlers();
        mainServerTCP.resetServer();
        MainTcp.interruptTCPConnectionAcceptor();
    }

    public static MainServerRMI getMainServerRMI(){
        return mainServerRMI;
    }
}
