package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class JoinedGameMessage extends GameHandlingMessage {
    private final String gameName;

    public JoinedGameMessage(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
