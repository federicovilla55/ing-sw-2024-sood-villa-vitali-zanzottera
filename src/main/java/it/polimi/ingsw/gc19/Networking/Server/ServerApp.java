package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MainServerTCP;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.TCPConnectionAcceptor;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ClientHandlerSocket;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * This class is used to start, run and close both {@link MainServerTCP}
 * and {@link MainServerRMI}. It permits to user to specify a (different) port
 * for TCP and RMI. IP address of {@link MainServerTCP} and {@link MainServerRMI}
 * is 127.0.0.1.
 */
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

    /**
     * This method is responsible for starting {@link MainServerRMI} on
     * port specified by the user. It builds a new registry, exports {@link MainServerRMI}
     * and bind it to the registry.
     * If {@link RemoteException} occurs while doing such operation
     * the system exits.
     * @param RMIPort the port on which start {@link MainServerRMI} (default is <code>1099</code>)
     */
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

    /**
     * Getter for {@link MainServerTCP} instanced.
     * @return the {@link MainServerTCP} built.
     */
    public static MainServerTCP getMainServerTCP(){
        return mainServerTCP;
    }

    /**
     * This method is used to start the {@link MainServerTCP}.
     * It starts {@link TCPConnectionAcceptor} on the specified port ({@param TCPPort})
     * and builds a new {@link MainServerTCP}
     * @param TCPPort the port on which start {@link MainServerTCP} (default is <code>25.0000</code>
     */
    public static void startTCP(int TCPPort){
        mainServerTCP = MainServerTCP.getInstance();
        TCPConnectionAcceptor = new TCPConnectionAcceptor(mainServerTCP, TCPPort);
        TCPConnectionAcceptor.start();
    }

    /**
     * This method is used when TCP server has to be torn down.
     * First, it kills {@link ClientHandlerSocket} associated to {@link MainServerTCP}
     * with {@link MainServerTCP#killClientHandlers()}, reset {@link MainServerTCP} with
     * {@link MainServerTCP#resetServer()} and, finally, it interrupts {@link TCPConnectionAcceptor}.
     */
    public static void stopTCP(){
        mainServerTCP.killClientHandlers();
        mainServerTCP.resetServer();
        TCPConnectionAcceptor.interruptTCPConnectionAcceptor();
    }

    /**
     * Getter for {@link MainServerRMI} instantiated.
     * @return the {@link MainServerRMI} instanced.
     */
    public static MainServerRMI getMainServerRMI(){
        return mainServerRMI;
    }

    /**
     * This method is used to un-export registry.
     * If {@link RemoteException} occurs when trying to do such thing
     * the system exits.
     */
    public static void unexportRegistry() {
        try {
            UnicastRemoteObject.unexportObject(mainServerRMI, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            System.out.println("[EXCEPTION]: RemoteException occurred while trying to un-export registry. Quitting...");
            System.exit(-1);
        }
    }

    /**
     * This method is used to tear down {@link MainServerRMI}.
     * First, it un-exports registry using {@link ServerApp#unexportRegistry()},
     * then it kills all {@link ClientHandlerRMI} using {@link MainServerRMI#killClientHandlers()}
     * and, finally, it reset {@link MainServerRMI} using {@link MainServerRMI#resetServer()}.
     */
    public static void stopRMI(){
        unexportRegistry();
        mainServerRMI.killClientHandlers();
        mainServerRMI.resetServer();
    }

}
