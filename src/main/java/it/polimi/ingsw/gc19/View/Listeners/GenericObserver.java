package it.polimi.ingsw.gc19.View.Listeners;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;

import java.util.ArrayList;

public class GenericObserver {
    
    public void notifyEvent(ArrayList<Message> messages) { }

    public void notifyEvent(LocalStationPlayer localStationPlayer) { }

    public void notifyEvent(LocalModel localModel) { }
    
    public void notifyConnection(String name) { }
    
    public void notifyDisconnection(String name) { }

    public void notifyReconnection(String name) { }
    
    public void notifyEvent(GameHandlingEvents type, String value) { }

    public void notifyColorAccepted(Color color) { }

    public void notifyInitialCard(CardOrientation cardOrientation) { }
    
    public void notifyGoalCard(GoalCard goalCard) { }

}
