package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class StartPlayingGameMessage extends NotifyEventOnGame {

    private final String nickFirstPlayer;

    public StartPlayingGameMessage(String nickFirstPlayer){
        this.nickFirstPlayer = nickFirstPlayer;
    }

    public String getNickFirstPlayer() {
        return this.nickFirstPlayer;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
