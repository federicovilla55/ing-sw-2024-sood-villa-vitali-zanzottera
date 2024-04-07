package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandlerRMI extends ClientHandler{

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String username, GameController gameController, MainController mainController) {
        super(username, gameController, mainController);
        this.virtualClientAssociated = virtualClientAssociated;
        new Thread(this::runHeartBeatReceiver).start();
    }

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String username) {
        super(username, null, null);
        this.virtualClientAssociated = virtualClientAssociated;
        new Thread(this::runHeartBeatReceiver).start();
    }

    public void runHeartBeatReceiver(){
        while(true) {
            try {
                ClientHandlerRMI.this.heartBeat();
            }
            catch(RemoteException remoteException){
                //@TODO: handle this exception
            }
        }
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {
        try{
            virtualClientAssociated.pushUpdate(message);
        }
        catch(RemoteException remoteException){
            //@TODO: handle this exception
        }
    }

    @Override
    public void heartBeat() throws RemoteException {
        long newDate;
        if(this.lastSignalFromClient == null){
            this.lastSignalFromClient = new Date().getTime();
        }
        else{
            newDate = new Date().getTime();
            if(newDate - this.lastSignalFromClient > 20){ //@TODO: fix this value
                this.mainController.setPlayerInactive(this.username);
            }
            else{
                this.lastSignalFromClient = newDate;
            }
        }
    }

}

