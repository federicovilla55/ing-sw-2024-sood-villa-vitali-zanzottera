package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public class ClientHandleRmi extends HandleClient {

    private final VirtualClient virtualClientAssociated;

    public ClientHandleRmi(VirtualClient virtualClientAssociated, String nickName){
        this.virtualClientAssociated = virtualClientAssociated;
        super.username = nickName;
    }
    @Override
    public void SendMessageToClient() {
        if(!super.messageQueue.isEmpty()){
            MessageToClient messageToSend;
            synchronized (super.messageQueue){
                messageToSend = super.messageQueue.poll();
            }
            virtualClientAssociated.GetMessage(messageToSend);
        }
    }
}
