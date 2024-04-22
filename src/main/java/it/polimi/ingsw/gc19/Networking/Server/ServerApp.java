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
import java.util.Scanner;

public class ServerApp {
    private static TCPConnectionAcceptor TCPConnectionAcceptor;
    private static MainServerTCP mainServerTCP;
    private static MainServerRMI mainServerRMI;
    private static Registry registry;

    public static void main(String[] args){
        boolean validPort = false;
        int RMIPort = Settings.DEFAULT_RMI_SERVER_PORT;
        int TCPPort = Settings.DEFAULT_TCP_SERVER_PORT;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Here yuo can start TCP and RMI server... \n");
        System.out.println("Insert port for  (or 'default'): ");

        while(!validPort){
            String portRMI = scanner.nextLine();
            if(portRMI.equals("default")){
                validPort = true;
            }
            else{
                try {
                    RMIPort = Integer.parseInt(scanner.nextLine());
                    if(RMIPort < 1 || RMIPort > 65353){
                        System.out.println("[ERROR] You must specify a valid port... \n");
                    }
                    else{
                        validPort = true;
                    }
                }
                catch (NumberFormatException numberFormatException){
                    System.out.println("[ERROR] You have to specify a valid port or 'default'... \n");
                }
            }
        }
        System.out.println("Starting RMI on IP " + Settings.DEFAULT_SERVER_IP + " and port " + validPort + "... \n");
        startRMI(RMIPort);

        validPort = false;
        while(!validPort){
            String portRMI = scanner.nextLine();
            if(portRMI.equals("default")){
                validPort = true;
            }
            else{
                try {
                    TCPPort = Integer.parseInt(scanner.nextLine());
                    if(TCPPort < 1 || TCPPort > 65353){
                        System.out.println("[ERROR] You must specify a valid port... \n");
                    }
                    else{
                        if(TCPPort == RMIPort){
                            System.out.println("[ERROR] TCP port and RMI port must be different... \n");
                        }
                        else{
                            validPort = true;
                        }
                    }
                }
                catch (NumberFormatException numberFormatException){
                    System.out.println("[ERROR] You have to specify a valid port or 'default'... \n");
                }
            }
        }
        System.out.println("Starting TCP on IP " + Settings.DEFAULT_SERVER_IP + " and port " + TCPPort);
        startTCP(TCPPort);
    }

    public static void startRMI(int RMIPort){
        mainServerRMI = MainServerRMI.getInstance();
        try {
            registry = LocateRegistry.createRegistry(RMIPort);
            VirtualMainServer stub = (VirtualMainServer) UnicastRemoteObject.exportObject(mainServerRMI, 0);
            registry.rebind(Settings.mainRMIServerName, stub);
        }
        catch (RemoteException remoteException){
            System.out.println("[EXCEPTION] RemoteException occurred while trying to start RMI Server. Quitting...");
            System.exit(-1);
        }
    }

    public static MainServerTCP getMainServerTCP(){
        return mainServerTCP;
    }

    public static void startTCP(int TCPPort){
        mainServerTCP = MainServerTCP.getInstance();
        TCPConnectionAcceptor = new TCPConnectionAcceptor(mainServerTCP, TCPPort);
        TCPConnectionAcceptor.start();
    }
    public static void stopTCP(){
        mainServerTCP.killClientHandlers();
        mainServerTCP.resetServer();
        TCPConnectionAcceptor.interruptTCPConnectionAcceptor();
    }

    public static MainServerRMI getMainServerRMI(){
        return mainServerRMI;
    }

    public static void unexportRegistry() {
        try {
            UnicastRemoteObject.unexportObject(mainServerRMI, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            System.out.println("[EXCEPTION]: RemoteException occurred while trying to un-export registry. Quitting...");
            System.exit(-1);
        }
    }

    public static void stopRMI(){
        mainServerRMI.killClientHandlers();
        mainServerRMI.resetServer();
    }

}
