package it.polimi.ingsw.gc19.Controller;

public class ClientPlayer {
    private final String name;
    private final String password;
    ClientPlayer(String name, String password){
        this.name = name;
        this.password = password;
    }
    String getName(){
        return this.name;
    }
    String getEncrypedPass()
    {
        return this.password;
    }
}
