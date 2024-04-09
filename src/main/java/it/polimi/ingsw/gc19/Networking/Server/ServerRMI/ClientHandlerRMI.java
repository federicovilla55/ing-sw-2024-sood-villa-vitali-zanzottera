package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientHandlerRMI extends ClientHandler{

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String username) {
        super(username, null);
        this.virtualClientAssociated = virtualClientAssociated;

    }

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, ClientHandler clientHandler) {
        super(clientHandler.getName(), clientHandler.getGameController());
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
        this.virtualClientAssociated = virtualClientAssociated;
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {
        try{
            //System.out.println(message.getClass() + "  " + message.getHeader());
            virtualClientAssociated.pushUpdate(message);
        }
        catch(RemoteException remoteException){
            System.out.println(remoteException.getMessage());
            System.out.println("Remote Exception -> " + message.getClass());
            //@TODO: handle this exception
        }
    }

}

