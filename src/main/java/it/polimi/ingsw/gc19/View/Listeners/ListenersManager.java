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
import java.util.Map;
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

    public void attacheListener(Listener listener){
        for(ListenerType l : ListenerType.values()){
            ArrayList<Listener> prev = new ArrayList<>(this.attachedListeners.getOrDefault(l, List.of()));
            prev.add(listener);
            this.attachedListeners.put(l, prev);
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
        for(Listener c : this.attachedListeners.get(ListenerType.CHAT_LISTENER)) {
            ((ChatListener) c).notify(messages);
        }
    }

    public void notifyErrorChatListener(String description){
        for(Listener c : this.attachedListeners.get(ListenerType.CHAT_LISTENER)) {
            ((ChatListener) c).notify(description);
        }
    }

    public void notifyGameStateListener(GameEvents gameEvents, List<String> varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((GameStateListener) c).notify(gameEvents, varArgs);
        }
    }

    public void notifyStationListener(GameEvents gameEvents, PersonalStation personalStation){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((StationListener) c).notify(gameEvents, personalStation);
        }
    }

    public void notifyErrorTableListener(String ... error){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((TableListener) c).notify(error);
        }
    }

    public void notifyStationListener(GameEvents gameEvents, OtherStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((StationListener) c).notify(gameEvents, otherStation);
        }
    }

    public void notifyErrorStationListener(String ... args){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((StationListener) c).notify(args);
        }
    }

    public void notifySetupListener(Color color){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(color);
        }
    }

    public void notifySetupListener(GoalCard goalCard){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(goalCard);
        }
    }

    public void notifySetupListener(PlayableCard initialCard){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(initialCard);
        }
    }

    public void notifyErrorSetupListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(error);
        }
    }

    public void notifyGameEventsListener(GameEvents gameEvents, LocalTable localTable){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((TableListener) c).notify(gameEvents, localTable);
        }
    }

    public void notifyErrorTurnStateListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.TURN_LISTENER)) {
            ((TurnStateListener) c).notify(error);
        }
    }

    public void notifyTurnStateListener(String nick, TurnState turnState){
        for(Listener c : this.attachedListeners.get(ListenerType.TURN_LISTENER)) {
            ((TurnStateListener) c).notify(nick, turnState);
        }
    }

    public void notifyGameHandlingListener(List<String> availableGames){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)) {
            ((GameHandlingListener) c).notify(availableGames);
        }
    }

    public void notifyGameHandlingListener(GameHandlingEvents type, List<String> varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)) {
            ((GameHandlingListener) c).notify(type, varArgs);
        }
    }

    public void notifyErrorGameHandlingListener(String errorDescription){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)) {
            ((GameHandlingListener) c).notify(errorDescription);
        }
    }

    public void notifyPlayerCreationListener(String name){
        for(Listener c : this.attachedListeners.get(ListenerType.PLAYER_CREATION_LISTENER)) {
            ((PlayerCreationListener) c).notify(name);
        }
    }

    public void notifyDisconnectionListener(String ... varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.DISCONNECTION_LISTENER)) {
            ((DisconnectionListener) c).notify(varArgs);
        }
        ((DisconnectionListener) this.attachedListeners.get(ListenerType.DISCONNECTION_LISTENER)).notify(varArgs);
    }

    public void notifySetupListener(List<Color> availableColors){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(availableColors);
        }
    }

}
