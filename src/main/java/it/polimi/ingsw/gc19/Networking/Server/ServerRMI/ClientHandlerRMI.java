package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.RemoteException;

public class ClientHandlerRMI extends ClientHandler{

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String nickName){
        super(nickName);
        this.virtualClientAssociated = virtualClientAssociated;
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        virtualClientAssociated.pushUpdate(message);
    }

}

