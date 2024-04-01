package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents.NotifyEventOnGame;

public class StartGameMessage extends NotifyEventOnGame {

    private final String nickFirstPlayer;

    public StartGameMessage(String nickFirstPlayer){
        this.nickFirstPlayer = nickFirstPlayer;
    }

    public String getNickFirstPlayer() {
        return this.nickFirstPlayer;
    }

}
