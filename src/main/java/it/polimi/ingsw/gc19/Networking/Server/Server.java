package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.MainController;

/**
 * This abstract class represents a generic server. In other words,
 * some "network interfaces" of the application
 */
public abstract class Server{

    protected final MainController mainController;

    protected Server(){
        this.mainController = MainController.getMainController();
    }

}
