package it.polimi.ingsw.gc19.Networking.Events.ClientEvents;

import it.polimi.ingsw.gc19.Networking.Events.Event;

public class CreateGameEvent extends Event {
    public String gameName;
    public int numPlayer;

    public CreateGameEvent(String gameName, int numPlayer)
    {
        this.gameName = gameName;
        this.numPlayer = numPlayer;
    }
}
