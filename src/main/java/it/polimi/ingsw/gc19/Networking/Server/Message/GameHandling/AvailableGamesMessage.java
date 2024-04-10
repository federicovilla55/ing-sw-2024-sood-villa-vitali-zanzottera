package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.ArrayList;
import java.util.List;

public class AvailableGamesMessage extends GameHandlingMessage{
    private final List<String> availableGames;

    public AvailableGamesMessage(List<String> availableGames){
        this.availableGames = availableGames;
    }

    public List<String> getAvailableGames() {
        return this.availableGames;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
