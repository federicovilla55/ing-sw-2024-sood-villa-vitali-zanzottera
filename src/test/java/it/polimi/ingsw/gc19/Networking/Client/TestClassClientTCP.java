package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.JoinGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;

public class TestClassClientTCP extends ClientTCP implements CommonClientMethodsForTests{

    public TestClassClientTCP(String nickname, MessageHandler messageHandler) throws IOException{
        super(nickname, messageHandler);


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
                if (waitAndNotifyTypeOfMessage(GameHandlingError.class, JoinedGameMessage.class) == 1) {
                    found = true;
                } else {
                    getMessage(GameHandlingError.class);
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
