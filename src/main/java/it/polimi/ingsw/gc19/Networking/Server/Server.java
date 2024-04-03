package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.Controller;

public abstract class Server{

    protected Controller controller;

    public void setController(Controller controller){
        this.controller = controller;
    }

    public Controller getController(){
        return this.controller;
    }

}
