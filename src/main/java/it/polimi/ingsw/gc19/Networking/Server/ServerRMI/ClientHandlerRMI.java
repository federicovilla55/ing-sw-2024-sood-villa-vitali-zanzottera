package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.rmi.RemoteException;

public class ClientHandlerRMI extends ClientHandler {

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String nickName){
        super(nickName);
        this.virtualClientAssociated = virtualClientAssociated;
    }

    @Override
    public void update(MessageToClient message) {
        //System.out.println("RMI send message");
        try {
            virtualClientAssociated.GetMessage(message);
        }
        catch (RemoteException ignored){ };
    }
}

