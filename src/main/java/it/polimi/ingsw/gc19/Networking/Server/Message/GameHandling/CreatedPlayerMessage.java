package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class CreatedPlayerMessage extends GameHandlingMessage{

    private final String nick;

    public CreatedPlayerMessage(String nick) {
        this.nick = nick;
    }

    public CreatedPlayerMessage(String nick) {
        this(nick, null);
    }

    public String getNick() {
        return this.nick;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof CreatedPlayerMessage)) return false;
        return ((CreatedPlayerMessage) o).nick.equals(this.nick);
    }

}
