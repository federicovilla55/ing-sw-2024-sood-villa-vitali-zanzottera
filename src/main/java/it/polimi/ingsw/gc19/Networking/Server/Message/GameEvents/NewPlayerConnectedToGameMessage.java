package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class NewPlayerConnectedToGameMessage extends GameHandlingMessage {

    private final String playerName;

    public NewPlayerConnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
