package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.RemoteException;

public class ClientHandleRmi extends HandleClient{

    private final VirtualClient virtualClientAssociated;

    public ClientHandleRmi(VirtualClient virtualClientAssociated, String nickName){
        this.virtualClientAssociated = virtualClientAssociated;
        super.username = nickName;
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {
        //System.out.println("RMI send message");
        try {
            virtualClientAssociated.GetMessage(message);
        }
        catch (RemoteException ignored){ };
    }
}

