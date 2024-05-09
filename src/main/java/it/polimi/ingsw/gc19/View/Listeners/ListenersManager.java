package it.polimi.ingsw.gc19.View.Listeners;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameStateEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameStateListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListsners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TurnStateListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListeners.StateListener;

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

    public void notifyGameStateListener(GameStateEvents gameStateEvents, List<String> varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((GameStateListener) c).notify(gameStateEvents, varArgs);
        }
    }

    public void notifyStationListener(GameStateEvents gameStateEvents, PersonalStation personalStation){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((StationListener) c).notify(gameStateEvents, personalStation);
        }
    }

    public void notifyErrorTableListener(String ... error){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((TableListener) c).notify(error);
        }
    }

    public void notifyStationListener(GameStateEvents gameStateEvents, OtherStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((StationListener) c).notify(gameStateEvents, otherStation);
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

    public void notifyGameEventsListener(GameStateEvents gameStateEvents, LocalTable localTable){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_EVENTS_LISTENER)) {
            ((TableListener) c).notify(gameStateEvents, localTable);
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

    public void notifyErrorPlayerCreationListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.PLAYER_CREATION_LISTENER)) {
            ((PlayerCreationListener) c).notifyError(error);
        }
    }

    public void notifySetupListener(List<Color> availableColors){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(availableColors);
        }
    }

    public void notifyStateListener(ViewState state){
        for(Listener l : this.attachedListeners.get(ListenerType.STATE_LISTENER)){
            ((StateListener) this.attachedListeners.get(ListenerType.STATE_LISTENER)).notify(state);
        }
    }

    public void notifyStateError(String error){
        for(Listener l : this.attachedListeners.get(ListenerType.STATE_LISTENER)){
            ((StateListener) this.attachedListeners.get(ListenerType.STATE_LISTENER)).notify(error);
        }
    }

}
