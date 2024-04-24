package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents the remote client seen by server.
 * It extends {@link Remote} so that methods can be called remotely.
 */
public interface VirtualClient extends Remote{

    /**
     * This remote method is used by server to remotely push
     * an update to the client.
     * @param message the {@link MessageToClient} to send to RMI client
     * @throws RemoteException if some errors occurs while calling this method
     */
    void pushUpdate(MessageToClient message) throws RemoteException;

}
