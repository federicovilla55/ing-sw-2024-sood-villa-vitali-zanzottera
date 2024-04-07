package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

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
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameEventsMessageVisitor) ((GameEventsMessageVisitor) visitor).visit(this);
    }

}
