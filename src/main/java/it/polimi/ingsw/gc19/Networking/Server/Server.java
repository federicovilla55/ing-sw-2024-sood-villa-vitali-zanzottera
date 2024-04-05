package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.MainServer;

public abstract class Server{

    protected MainServer mainServer;

    public void setController(MainServer mainServer){
        this.mainServer = mainServer;
    }

    public MainServer getController(){
        return this.mainServer;
    }

}
