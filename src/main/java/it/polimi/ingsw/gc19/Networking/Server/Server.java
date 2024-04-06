package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.MainController;

public abstract class Server{

    protected MainController mainController;

    public void setController(MainController mainController){
        this.mainController = mainController;
    }

    public MainController getController(){
        return this.mainController;
    }

}
