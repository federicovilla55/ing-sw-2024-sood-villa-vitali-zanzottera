package it.polimi.ingsw.gc19.Networking.RMI.Socket.TempFolderToTestServer;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRmiTest extends UnicastRemoteObject implements VirtualClient {
    final VirtualGameServer server;
    protected ClientRmiTest(VirtualGameServer server) throws RemoteException {
        this.server = server;
    }

    private void run() {
        try {
            this.server.newConnection(this, "Aryan");
        } catch (RemoteException e) {
            System.err.println("Name Already present");
        }
    }

    public static void main(String[] args)  throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(args[0], 12122);
        VirtualGameServer server = (VirtualGameServer) registry.lookup("RMIServer");

        new ClientRmiTest(server).run();
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException{

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
}