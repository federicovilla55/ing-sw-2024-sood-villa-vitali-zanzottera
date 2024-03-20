package it.polimi.ingsw.gc19.Controller;

public class ClientPlayer {
    private final String name;
    private final String SecretKey;
    public String gamePlay;
    private String nickname;

    ClientPlayer(String name, String password){
        this.name = name;
        this.SecretKey = "ciao";
        this.nickname = null;
        gamePlay = null;
    }
    public String getName(){
        return this.name;
    }
    public String getGameName() { return this.nickname; }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

}
