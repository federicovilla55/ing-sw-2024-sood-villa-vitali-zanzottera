package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;
import it.polimi.ingsw.gc19.Networking.ToFix.ClientImpl.ServerImpl.ClientHandle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientHandleSocket extends HandleClient implements VirtualServer, Runnable{
    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private Controller MasterController;
    public ClientHandleSocket(Socket clientSocket, Controller MasterController) throws IOException
    {
        this.clientSocket = clientSocket;
        //this.nickName = null;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new  ObjectInputStream(clientSocket.getInputStream());
        //this.getLastTimeStep = System.currentTimeMillis();
        this.MasterController = MasterController;
    }

    public void run() {

    }

    @Override
    public void NewConnection(VirtualClient client, String nickName) throws RemoteException {

    }

    @Override
    public void NewUser(String nickname) throws RemoteException {

    }

    @Override
    public void CreateGame(String nickName, String gameName, int numPlayer) throws RemoteException {

    }

    @Override
    public void JoinGame(String nickName, String GameName) throws RemoteException {

    }

    @Override
    public void PlaceCard(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException {

    }


    @Override
    public void HeartBeat(String nickName) throws RemoteException {

    }
    @Override
    public void Reconnect() throws RemoteException {

    }

    @Override
    public void SendChatTo(String nickName, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException {

    }
}
