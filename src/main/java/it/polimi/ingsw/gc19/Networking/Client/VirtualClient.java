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

public interface VirtualClient extends Remote{

    void pushUpdate(MessageToClient message) throws RemoteException;

    /*
    void pushUpdate(AnswerToActionMessage answerToActionMessage) throws RemoteException;
    void pushUpdate(NotifyChatMessage notifyChatMessage) throws RemoteException;
    void pushUpdate(ConfigurationMessage configurationMessage) throws RemoteException;
    void pushUpdate(NotifyEventOnGame notifyEventOnGame) throws RemoteException;
    void pushUpdate(GameHandlingMessage gameHandlingMessage) throws RemoteException;
    void pushUpdate(TurnStateMessage turnStateMessage) throws RemoteException;*/

}
