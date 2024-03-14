package it.polimi.ingsw.gc19.Model.Player;

import it.polimi.ingsw.gc19.Controller.ClientPlayer;
import it.polimi.ingsw.gc19.Model.Enums.Color;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.ArrayList;

public class Player {
    private final String name;
    private Color playerColor;
    private Station playerStation;

    private final ClientPlayer Client;

    public Player(String name, ClientPlayer Client){
        this.name = name;
        this.playerStation = new Station();
        this.Client = Client;
    }

    public Station getPlayerStation() {return this.playerStation; }

    public String getName(){
        return  this.name;
    }

    public void setColor(Color color){
        this.playerColor = color;
    }

    public Color getColor(){
        return this.playerColor;
    }

    public ClientPlayer getClient()
    {
        return this.Client;
    }

}
