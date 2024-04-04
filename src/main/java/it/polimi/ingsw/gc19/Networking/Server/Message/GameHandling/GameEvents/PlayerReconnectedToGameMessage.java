package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

public class PlayerReconnectedToGameMessage extends NotifyEventOnGame{
    private final String playerName;

    public PlayerReconnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

}
