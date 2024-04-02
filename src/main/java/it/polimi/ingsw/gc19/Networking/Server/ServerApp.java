package it.polimi.ingsw.gc19.Networking.Server;


import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Server.ServerRmi.ServerRmi;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.ServerTcp;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.lang.Math.abs;

public class ServerApp {

    public static List<HandleClient> ActiveClient;
    public static Controller MasterController;

    public static long MAXTIME = 10000;
    public static void main(String[] args) throws IOException {
        List<HandleClient> ListClient = new ArrayList<HandleClient>();;
        List<HandleClient> ListNonActiveClient = new ArrayList<HandleClient>();
        Controller MasterController = new Controller();
        VirtualServer MainRmi = new ServerRmi(ListClient,MasterController);
        VirtualServer stub = (VirtualServer) UnicastRemoteObject.exportObject(MainRmi, 0);
        Registry registry = LocateRegistry.createRegistry(12122);
        registry.rebind("RMIServer", stub);
        ServerTcp MainTcp = new ServerTcp(ListClient, MasterController);
    }

    public void checkActiveClient()
    {
        for(HandleClient client : ActiveClient){
            if(client.getName() != null && abs(client.getGetLastTimeStep()-System.currentTimeMillis()) > MAXTIME) {
                //MasterController.SetToNonActive(client.getName());
                //NonActiveClient.add(client);
                ActiveClient.remove(client);
            }
        }
    }

}
