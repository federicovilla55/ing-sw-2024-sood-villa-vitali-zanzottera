package it.polimi.ingsw.gc19.View.Listeners;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.ChatListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameStateListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.DisconnectionListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListsners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.TurnStateListeners.TurnStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ListenersManager {

    private final ConcurrentHashMap<ListenerType, List<Listener>> attachedListeners;

    public ListenersManager(){
        this.attachedListeners = new ConcurrentHashMap<>();
    }

    public void attachListener(ListenerType type, Listener listener){
        if(this.attachedListeners.containsKey(type)){
            this.attachedListeners.get(type).add(listener);
        }
        else{
            this.attachedListeners.put(type, List.of(listener));
        }
    }

    public void removeListener(ListenerType type, Listener listener){
        this.attachedListeners.get(type).remove(listener);
    }

    public void removeListener(Listener listener){
        for(ListenerType l : this.attachedListeners.keySet()){
            this.attachedListeners.get(l).remove(listener);
        }
    }

    public void notifyChatListener(ArrayList<Message> messages){
        ((ChatListener) this.attachedListeners.get(ListenerType.CHAT_LISTENER)).notify(messages);
    }

    public void notifyGameEventsListener(GameEvents gameEvents, List<String> varArgs){
        ((GameStateListener) this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)).notify(gameEvents, varArgs);
    }

    public void notifyGameEventsListener(GameEvents gameEvents, PersonalStation personalStation){
        ((StationListener) this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)).notify(gameEvents, personalStation);
    }

    public void notifyGameEventsListener(GameEvents gameEvents, OtherStation otherStation){
        ((StationListener) this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)).notify(gameEvents, otherStation);
    }

    public void notifySetupListener(Color color){
        ((SetupListener) this.attachedListeners.get(ListenerType.SETUP_LISTENER)).notify(color);
    }

    public void notifySetupListener(GoalCard goalCard){
        ((SetupListener) this.attachedListeners.get(ListenerType.SETUP_LISTENER)).notify(goalCard);
    }

    public void notifySetupListener(PlayableCard initialCard){
        ((SetupListener) this.attachedListeners.get(ListenerType.SETUP_LISTENER)).notify(initialCard);
    }

    public void notifyGameEventsListener(GameEvents gameEvents, LocalTable localTable){
        ((TableListener) this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)).notify(gameEvents, localTable);
    }

    public void notifyTurnStateListener(String nick, TurnState turnState){
        ((TurnStateListener) this.attachedListeners.get(ListenerType.TURN_LISTENER)).notify(nick, turnState);
    }

    public void notifyGameHandlingListener(List<String> availableGames){
        ((GameHandlingListener) this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)).notify(availableGames);
    }

    public void notifyGameHandlingListener(GameHandlingEvents type, List<String> varArgs){
        ((GameHandlingListener) this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)).notify(type, varArgs);
    }

    public void notifyPlayerCreationListener(String name){
        ((PlayerCreationListener) this.attachedListeners.get(ListenerType.PLAYER_CREATION_LISTENER)).notify(name);
    }

    public void notifyDisconnectionListener(String ... varArgs){
        ((DisconnectionListener) this.attachedListeners.get(ListenerType.DISCONNECTION_LISTENER)).notify(varArgs);
    }

    public void notifySetupListener(List<Color> availableColors){
        ((SetupListener) this.attachedListeners.get(ListenerType.SETUP_LISTENER)).notify(availableColors);
    }

}
