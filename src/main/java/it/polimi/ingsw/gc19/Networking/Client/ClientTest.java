package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientTest{

    public static void main(String[] args) throws RemoteException, NotBoundException {

        Registry registry = LocateRegistry.getRegistry("RMIServer", 12122);
        VirtualServer virtualServer = (VirtualServer) registry.lookup("RMIServer");

        Scanner scanner = new Scanner(System.in);
        it.polimi.ingsw.gc19.Networking.Client.Client client1 = new it.polimi.ingsw.gc19.Networking.Client.Client(virtualServer);
        it.polimi.ingsw.gc19.Networking.Client.Client client2 = new it.polimi.ingsw.gc19.Networking.Client.Client(virtualServer);
        client1.connect("Matteo");
        client1.newUser("Matteo");
        client2.connect("Mario");
        client2.newUser("Mario");
        client1.newGame("Matteo", "Game");
        client2.joinGame("Mario", "Game");
        client1.sendChatMessage("Matteo", new ArrayList<>(List.of("Mario", "Matteo")), "Ciao!!!");

    }

}

class Client extends UnicastRemoteObject implements VirtualClient, Serializable{

    private final VirtualServer virtualServer;

    public Client(VirtualServer virtualServer) throws RemoteException {
        super();
        this.virtualServer = virtualServer;
    }

    @Override
    public void GetMessage(MessageToClient message) {
        System.out.println("FOUND SOMETHING!!!!!!");
        if(message instanceof NotifyChatMessage){
            System.out.println(((NotifyChatMessage) message).getMessage());
        }
    }

    public void connect(String name) throws RemoteException {
        this.virtualServer.NewConnection(this, name);
    }

    public void newUser(String name) throws RemoteException {
        System.out.println("-----------------------");
        this.virtualServer.NewUser(name);
    }

    public void newGame(String nick, String gameName) throws RemoteException{
        this.virtualServer.CreateGame(nick, gameName, 2);
    }

    public void joinGame(String nick, String game) throws RemoteException {
        this.virtualServer.JoinGame(nick, game);
    }

    public void sendChatMessage(String nick, ArrayList<String> receivers, String message) throws RemoteException {
        this.virtualServer.SendChatTo(nick, receivers, message);
    }
}
