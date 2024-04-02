package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.ToFix.ClientImpl.ServerImpl.ClientHandle;

public class ClientHandleRmi extends HandleClient {

    private VirtualClient virtualClientAssociated;

    public ClientHandleRmi(VirtualClient virtualClientAssociated, String nickName){
        this.virtualClientAssociated = virtualClientAssociated;
        super.Username = nickName;
    }
    @Override
    public void SendMessageToClient() {
        if(!super.MessageQueue.isEmpty())
        {
            MessageToClient messageToSend;
            synchronized (super.MessageQueue){
                messageToSend = super.MessageQueue.poll();
            }
            virtualClientAssociated.GetMessage(messageToSend);
        }
    }
}
