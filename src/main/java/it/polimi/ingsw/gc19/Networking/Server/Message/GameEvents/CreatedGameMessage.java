package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class CreatedGameMessage extends GameHandlingMessage{

    private final String gameName;

    public CreatedGameMessage(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return this.gameName;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
