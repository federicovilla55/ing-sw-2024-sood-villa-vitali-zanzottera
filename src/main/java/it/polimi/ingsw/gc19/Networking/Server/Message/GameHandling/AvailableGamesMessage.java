package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

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
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
