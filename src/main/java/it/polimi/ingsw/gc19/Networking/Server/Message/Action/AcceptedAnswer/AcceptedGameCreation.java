package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

public class AcceptedGameCreation extends AcceptedActionMessage{
    private final String gameName;

    public AcceptedGameCreation(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return this.gameName;
    }

}
