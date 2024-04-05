package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class JoinedGameMessage extends GameHandlingMessage {
    private final String gameName;

    public JoinedGameMessage(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
