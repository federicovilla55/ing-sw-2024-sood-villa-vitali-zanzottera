package it.polimi.ingsw.gc19.View.Listeners;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.ChatListeners.Chat;
import it.polimi.ingsw.gc19.View.Listeners.ChatListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameStateListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListsners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.TurnStateListeners.TurnStateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ListenersManager {

    private final ConcurrentHashMap<Event, List<Listener>> attachedListeners;

    public ListenersManager(){
        this.attachedListeners = new ConcurrentHashMap<>();
    }

    public void notify(ArrayList<Message> messages){
        ((ChatListener) this.attachedListeners.get(Event.CHAT)).notify(messages);
    }

    public void notify(GameEvents gameEvents){
        ((GameStateListener) this.attachedListeners.get(Event.GAME_EVENTS)).notify(gameEvents);
    }

    public void notify(GameEvents gameEvents, PersonalStation personalStation){
        ((StationListener) this.attachedListeners.get(Event.GAME_EVENTS)).notify(gameEvents, personalStation);
    }

    public void notify(GameEvents gameEvents, OtherStation otherStation){
        ((StationListener) this.attachedListeners.get(Event.GAME_EVENTS)).notify(gameEvents, otherStation);
    }

    public void notify(Color color){
        ((SetupListener) this.attachedListeners.get(Event.SETUP)).notify(color);
    }

    public void notify(GoalCard goalCard){
        ((SetupListener) this.attachedListeners.get(Event.SETUP)).notify(goalCard);
    }

    public void notify(CardOrientation cardOrientation){
        ((SetupListener) this.attachedListeners.get(Event.SETUP)).notify(cardOrientation);
    }

    public void notify(GameEvents gameEvents, LocalTable localTable){
        ((TableListener) this.attachedListeners.get(Event.GAME_EVENTS)).notify(gameEvents, localTable);
    }

    public void notify(String nickFirstPlayer){
        //To notify view to place black pawn
        ((TurnStateListener) this.attachedListeners.get(Event.TURN)).notify(nickFirstPlayer);
    }

    public void notify(String nick, TurnState turnState){
        ((TurnStateListener) this.attachedListeners.get(Event.TURN)).notify(nick, turnState);
    }

    public void notify(GameHandlingEvents type, List<String> varArgs){
        ((GameHandlingListener) this.attachedListeners.get(Event.GAME_HANDLING_EVENTS)).notify(type, varArgs);
    }

}
