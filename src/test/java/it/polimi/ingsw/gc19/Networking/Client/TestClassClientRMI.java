package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.rmi.RemoteException;

public class TestClassClientRMI extends ClientRMI implements CommonClientMethodsForTests, ClientInterface{

    public TestClassClientRMI(MessageHandler messageHandler, ClientController clientController) throws RemoteException {
        super(messageHandler, clientController);
    }

    @Override
    public void waitForMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.getMessageHandler().getMessagesToHandle()) {
            while (this.getMessageHandler().getMessagesToHandle().stream().noneMatch(messageToClientClass::isInstance)) {
                try {
                    this.getMessageHandler().getMessagesToHandle().wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    @Override
    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.getMessageHandler().getMessagesToHandle()) {
            while (!this.getMessageHandler().getMessagesToHandle().isEmpty()) {
                MessageToClient res = this.getMessageHandler().getMessagesToHandle().remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

    @Override
    public String getNickname() {
        return super.getNickname();
    }

}