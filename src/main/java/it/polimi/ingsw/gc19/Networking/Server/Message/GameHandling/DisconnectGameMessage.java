package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class DisconnectGameMessage extends GameHandlingMessage {
    private final String gameName;

    public DisconnectGameMessage(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
