package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class InsertCardEvent extends Event {

    public String gameName;
    public String nickname;
    public int x;
    public int y;

    InsertCardEvent(String nickname, String gameName , int x,int y){
        this.nickname = nickname;
        this.gameName = gameName;
        this.x = x;
        this.y = y;
    }

}
