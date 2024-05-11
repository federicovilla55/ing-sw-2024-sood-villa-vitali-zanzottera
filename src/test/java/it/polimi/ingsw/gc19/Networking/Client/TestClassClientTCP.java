package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.JoinGameMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;

public class TestClassClientTCP extends ClientTCP implements CommonClientMethodsForTests, ClientInterface{

    public TestClassClientTCP(MessageHandler messageHandler, ClientController clientController) throws IOException{
        super(messageHandler);
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

    public int waitAndNotifyTypeOfMessage(Class<? extends MessageToClient> messageToClientClass1, Class<? extends MessageToClient> messageToClientClass2) {
        synchronized (this.getMessageHandler().getMessagesToHandle()) {
            while (this.getMessageHandler().getMessagesToHandle().stream().noneMatch(messageToClientClass1::isInstance) &&
                    this.getMessageHandler().getMessagesToHandle().stream().noneMatch(messageToClientClass2::isInstance)) {
                try {
                    this.getMessageHandler().getMessagesToHandle().wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.getMessageHandler().getMessagesToHandle().stream().noneMatch(messageToClientClass1::isInstance)){
                return 1;
            }
            return 0;
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

    public void joinGame(String gameName, boolean wait){
        if(wait){
            boolean found = false;
            while (!found) {
                this.sendMessage(new JoinGameMessage(gameName, this.getNickname()));
                if (waitAndNotifyTypeOfMessage(GameHandlingErrorMessage.class, JoinedGameMessage.class) == 1) {
                    found = true;
                } else {
                    getMessage(GameHandlingErrorMessage.class);
                }
            }
        }else {
            joinGame(gameName);
        }
    }

    @Override
    public String getNickname(){
        return super.getNickname();
    }

}
