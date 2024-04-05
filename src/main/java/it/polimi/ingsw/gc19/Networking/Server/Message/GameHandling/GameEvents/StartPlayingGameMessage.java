package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class StartPlayingGameMessage extends NotifyEventOnGame {

    private final String nickFirstPlayer;

    public StartPlayingGameMessage(String nickFirstPlayer){
        this.nickFirstPlayer = nickFirstPlayer;
    }

    public String getNickFirstPlayer() {
        return this.nickFirstPlayer;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
