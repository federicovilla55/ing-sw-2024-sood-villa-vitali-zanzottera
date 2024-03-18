package it.polimi.ingsw.gc19.Controller;

public class ClientPlayer {
    private final String name;
    private boolean isActive;

    public ClientPlayer(String name){
        this.name = name;
        this.isActive = true;
    }

    public String getNickname() { return this.name; }

    public boolean getIsActive() { return  this.isActive; }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}
