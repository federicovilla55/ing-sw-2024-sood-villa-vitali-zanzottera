package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.MainController;

public abstract class Server{

    protected final MainController mainController;

    protected Server(){
        this.mainController = MainController.getMainServer();
    }

}
