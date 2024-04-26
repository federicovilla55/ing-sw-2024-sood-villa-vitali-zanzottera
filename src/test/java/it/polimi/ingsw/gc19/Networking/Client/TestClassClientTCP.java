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

public class TestClassClientTCP extends ClientTCP {

    public TestClassClientTCP(String nickname){
        super(nickname);
    }


    @Override
    public void waitForMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public int waitAndNotifyTypeOfMessage(Class<? extends MessageToClient> messageToClientClass1, Class<? extends MessageToClient> messageToClientClass2) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance) && this.incomingMessages.stream().noneMatch(messageToClientClass2::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance)){
                return 1;
            }
            return 0;
        }
    }

    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    @Override
    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (!this.incomingMessages.isEmpty()) {
                MessageToClient res = this.incomingMessages.remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

    public void joinGame(String gameName, boolean wait){
        if(wait){
            boolean found = false;
            while (!found) {
                this.sendMessage(new JoinGameMessage(gameName, this.nickname));
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

}
