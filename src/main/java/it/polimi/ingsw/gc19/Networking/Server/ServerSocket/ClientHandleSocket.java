package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;
import it.polimi.ingsw.gc19.Networking.ToFix.ClientImpl.ServerImpl.ClientHandle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

public class ClientHandleSocket extends HandleClient implements VirtualServer{
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

    //void run() {

    //}

    @Override
    public void NewUser() throws RemoteException {

    }

    @Override
    public void CreateGame() throws RemoteException {

    }

    @Override
    public void JoinGame() throws RemoteException {

    }

    @Override
    public void PlaceCard() throws RemoteException {

    }

    @Override
    public void HeartBeat() throws RemoteException {

    }

    @Override
    public void Reconnect() throws RemoteException {

    }
}
