package it.polimi.ingsw.gc19.Networking.RMI.Socket;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
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
        Client client1 = new Client(virtualServer, "Matteo");
        Client client2 = new Client(virtualServer, "Mario");
        client1.connect("Matteo");
        client2.connect("Mario");
        client1.newGame("Matteo", "Game");
        client2.joinGame("Mario", "Game");
        //client2.joinGame("Mario", "Game1");
        client1.sendChatMessage("Matteo", new ArrayList<>(List.of("Mario", "Matteo")), "Ciao!!!");
        client1.disconnect("Matteo");
        client1.reconnect("Matteo", "Game");


    }

}

class Client extends UnicastRemoteObject implements VirtualClient, Serializable{

    private final VirtualServer virtualServer;

    private final String name;

    public Client(VirtualServer virtualServer, String name) throws RemoteException {
        super();
        this.virtualServer = virtualServer;
        this.name = name;
    }

    public String getName() {
        return null;
    }

    @Override
    public void pushUpdate(MessageToClient message) {
        //System.out.println("FOUND SOMETHING!!!!!!");
        System.out.println(name + " received " + message.getClass() + "  ->  " + message);
        /*if(message instanceof NotifyChatMessage){
            System.out.println(((NotifyChatMessage) message).getMessage());
        }*/
    }

    @Override
    public void pushUpdate(AnswerToActionMessage answerToActionMessage) throws RemoteException {

    }

    @Override
    public void pushUpdate(NotifyChatMessage notifyChatMessage) throws RemoteException {

    }

    @Override
    public void pushUpdate(ConfigurationMessage configurationMessage) throws RemoteException {

    }

    @Override
    public void pushUpdate(NotifyEventOnGame notifyEventOnGame) throws RemoteException {

    }

    @Override
    public void pushUpdate(GameHandlingMessage gameHandlingMessage) throws RemoteException {

    }

    @Override
    public void pushUpdate(TurnStateMessage turnStateMessage) throws RemoteException {

    }

    public void connect(String name) throws RemoteException {
        this.virtualServer.newConnection(this, name);
    }

    public void newGame(String nick, String gameName) throws RemoteException{
        this.virtualServer.createGame(this, gameName, 2);
    }

    public void joinGame(String nick, String game) throws RemoteException {
        this.virtualServer.joinGame(this, game);
    }

    public void sendChatMessage(String nick, ArrayList<String> receivers, String message) throws RemoteException {
        this.virtualServer.sendChatMessage(this, receivers, message);
    }

    public void disconnect(String nick) throws RemoteException{
        this.virtualServer.disconnect(this, nick);
    }

    public void reconnect(String nick, String gameName) throws RemoteException{
        this.virtualServer.reconnect(this, gameName, nick);
    }
}
