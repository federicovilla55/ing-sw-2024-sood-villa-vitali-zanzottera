package it.polimi.ingsw.gc19.View.Listeners;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.*;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

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

    public void attachListener(Listener listener){
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

    public void notifyLocalModelListener(LocalModelEvents type, LocalModel localModel, String ... varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.LOCAL_MODEL_LISTENER)) {
            ((LocalModelListener) c).notify(type, localModel, varArgs);
        }
    }

    public void notifyStationListener(PersonalStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notify(otherStation);
        }
    }

    public void notifyStationListener(OtherStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notify(otherStation);
        }
    }

    public void notifyErrorStationListener(String ... args){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notifyErrorStation(args);
        }
    }

    public void notifySetupListener(SetupEvent type){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(type);
        }
    }

    public void notifyErrorSetupListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(error);
        }
    }

    public void notifyTableListener(LocalTable localTable){
        for(Listener c : this.attachedListeners.get(ListenerType.TABLE_LISTENER)) {
            ((TableListener) c).notify(localTable);
        }
    }

    public void notifyTurnStateListener(String nick, TurnState turnState){
        for(Listener c : this.attachedListeners.get(ListenerType.TURN_LISTENER)) {
            ((TurnStateListener) c).notify(nick, turnState);
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
            ((PlayerCreationListener) c).notifyPlayerCreationError(error);
        }
    }

    public void notifyStateListener(ViewState viewState){
        for(Listener l : this.attachedListeners.get(ListenerType.STATE_LISTENER)){
            ((StateListener) l).notify(viewState);
        }
    }

}
