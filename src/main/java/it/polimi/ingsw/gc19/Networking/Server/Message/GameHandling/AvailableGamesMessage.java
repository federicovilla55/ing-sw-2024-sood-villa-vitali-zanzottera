package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.ArrayList;

public class AvailableGamesMessage extends GameHandlingMessage{
    private final ArrayList<String> availableGames;

    public AvailableGamesMessage(ArrayList<String> availableGames){
        this.availableGames = availableGames;
    }

    public ArrayList<String> getAvailableGames() {
        return this.availableGames;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
