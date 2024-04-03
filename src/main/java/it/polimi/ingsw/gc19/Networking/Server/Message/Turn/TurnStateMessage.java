package it.polimi.ingsw.gc19.Networking.Server.Message.Turn;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public class TurnStateMessage extends MessageToClient{

    private final String nick;
    private final TurnState turnState;

    public TurnStateMessage(String nick, TurnState turnState){
        this.nick = nick;
        this.turnState = turnState;
    }

    public String getNick(){
        return this.nick;
    }

    public TurnState getTurnState(){
        return this.turnState;
    }

}
