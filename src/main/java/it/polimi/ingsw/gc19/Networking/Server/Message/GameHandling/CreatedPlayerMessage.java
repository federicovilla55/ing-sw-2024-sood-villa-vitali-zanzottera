package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class CreatedPlayerMessage extends GameHandlingMessage{

    private final String nick;

    public CreatedPlayerMessage(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
