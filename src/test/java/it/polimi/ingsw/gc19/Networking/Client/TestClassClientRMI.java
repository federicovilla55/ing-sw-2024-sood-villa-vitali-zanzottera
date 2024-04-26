package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;

import java.rmi.RemoteException;

public class TestClassClientRMI extends ClientRMI implements CommonClientMethodsForTests{

    public TestClassClientRMI(VirtualMainServer virtualMainServer, MessageHandler messageHandler, String nickname) throws RemoteException {
        super(virtualMainServer, messageHandler, nickname);
    }

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

    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.getMessageHandler().getMessagesToHandle()) {
            while (!this.getMessageHandler().getMessagesToHandle().isEmpty()) {
                MessageToClient res = this.getMessageHandler().getMessagesToHandle().remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

}
