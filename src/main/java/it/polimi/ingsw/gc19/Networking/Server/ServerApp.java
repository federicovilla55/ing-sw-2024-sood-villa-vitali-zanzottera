package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MainServerTCP;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.TCPConnectionAcceptor;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ClientHandlerSocket;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;
import it.polimi.ingsw.gc19.Utils.IPChecker;

import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
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

    /**
     * Connected {@link TCPConnectionAcceptor}
     */
    private static TCPConnectionAcceptor TCPConnectionAcceptor;

    /**
     * The {@link MainServerRMI} for this server
     */
    private static MainServerTCP mainServerTCP;

    /**
     * The {@link MainServerTCP} for this server
     */
    private static MainServerRMI mainServerRMI;

    /**
     * The RMI registry used by the server
     */
    private static Registry registry;

    public static void main(String[] args){
        boolean valid = false;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Here you can start TCP and RMI server... \n");

        System.out.println("Insert name for RMI server (" + ServerSettings.DEFAULT_RMI_SERVER_NAME + "):");
        String rmiName = scanner.nextLine();
        if(!rmiName.isEmpty()){
            ServerSettings.MAIN_RMI_SERVER_NAME = rmiName;
        }

        System.out.println("Insert RMI server IP (" + ServerSettings.MAIN_TCP_SERVER_IP +  "):");

        while(!valid){
            String ipRMI = scanner.nextLine();
            if(ipRMI.isEmpty()){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ipRMI);
                if(valid){
                    ServerSettings.MAIN_RMI_SERVER_IP = ipRMI;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP:");
                }
            }
        }

        System.setProperty("java.rmi.server.hostname", ServerSettings.MAIN_RMI_SERVER_IP);

        System.out.println("Insert port for RMI (" + ServerSettings.DEFAULT_RMI_SERVER_PORT + "):");

        valid = false;
        while(!valid){
            String portRMI = scanner.nextLine();
            if(portRMI.isEmpty()){
                valid = true;
            }
            else{
                if (IPChecker.checkPort(portRMI)) {
                    ServerSettings.RMI_SERVER_PORT = Integer.parseInt(portRMI);
                    valid = true;
                }
                else{
                    System.out.println("Error: invalid port! Enter a valid port:");
                }
            }
        }

        System.out.println("Starting RMI on IP " + ServerSettings.MAIN_RMI_SERVER_IP + " and port " + ServerSettings.RMI_SERVER_PORT + "... \n");
        startRMI();

        System.out.println("Insert IP for TCP (" + ServerSettings.DEFAULT_SERVER_IP +"):");

        valid = false;
        while(!valid){
            String ipTCP = scanner.nextLine();
            if(ipTCP.isEmpty()){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ipTCP);
                if(valid){
                    ServerSettings.MAIN_TCP_SERVER_IP = ipTCP;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP:");
                }
            }
        }

        System.out.println("Insert port for TCP (" + ServerSettings.DEFAULT_TCP_SERVER_PORT + "):");

        valid = false;
        while(!valid){
            String portTCP = scanner.nextLine();
            if(portTCP.isEmpty()){
                valid = true;
            }
            else{
                if (IPChecker.checkPort(portTCP)) {
                    ServerSettings.TCP_SERVER_PORT = Integer.parseInt(portTCP);
                    if(ServerSettings.RMI_SERVER_PORT != ServerSettings.TCP_SERVER_PORT){
                        valid = true;
                    }
                    else{
                        if(ServerSettings.MAIN_RMI_SERVER_IP.equals(ServerSettings.MAIN_TCP_SERVER_IP)){
                            System.out.println("Error: when IP of servers are the same, RMI port and TCP port must be different. Enter another port: ");
                        }
                        else{
                            valid = true;
                        }
                    }
                }
                else{
                    System.out.println("Error: invalid port! Enter a valid port:");
                }
            }
        }

        System.out.println("Starting TCP on IP " + ServerSettings.MAIN_TCP_SERVER_IP + " and port " + ServerSettings.TCP_SERVER_PORT + "... \n");
        startTCP();
    }

    /**
     * This method is responsible for starting {@link MainServerRMI} on
     * port specified by the user. It builds a new registry, exports {@link MainServerRMI}
     * and bind it to the registry.
     * If {@link RemoteException} occurs while doing such operation
     * the system exits.
     */
    public static void startRMI(){
        mainServerRMI = new MainServerRMI();
        try {
            registry = LocateRegistry.createRegistry(ServerSettings.RMI_SERVER_PORT);
            VirtualMainServer stub = (VirtualMainServer) UnicastRemoteObject.exportObject(mainServerRMI, 0);
            registry.rebind(ServerSettings.MAIN_RMI_SERVER_NAME, stub);
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
     * It starts {@link TCPConnectionAcceptor} on the specified port (<code>TCPPort</code>>)
     * and builds a new {@link MainServerTCP}
     */
    public static void startTCP(){
        mainServerTCP = new MainServerTCP();
        TCPConnectionAcceptor = new TCPConnectionAcceptor(mainServerTCP, ServerSettings.TCP_SERVER_PORT);
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
            registry.unbind(ServerSettings.MAIN_RMI_SERVER_NAME);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException | AccessException | NotBoundException e) {
            System.out.println("[EXCEPTION]: exception occurred while trying to un-export registry. Quitting...");
            System.exit(-1);
        } catch (RemoteException e){
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
