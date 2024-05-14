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

/**
 * This class manages listeners of other classes. Listeners can dynamically
 * register and unregister themselves from it.
 */
public class ListenersManager {

    private final ConcurrentHashMap<ListenerType, List<Listener>> attachedListeners;

    public ListenersManager(){
        this.attachedListeners = new ConcurrentHashMap<>();
    }

    /**
     * This method register the {@param listener} to {@param type} updates
     * @param type a {@link ListenerType} representing type of updates {@param listener} needs to listen
     * @param listener the {@link Listener} to register
     */
    public void attachListener(ListenerType type, Listener listener){
        if(this.attachedListeners.containsKey(type)){
            this.attachedListeners.get(type).add(listener);
        }
        else{
            this.attachedListeners.put(type, List.of(listener));
        }
    }

    /**
     * This method is used to register a listener to all updates lists
     * @param listener the {@link Listener} to register
     */
    public void attachListener(Listener listener){
        for(ListenerType l : ListenerType.values()){
            ArrayList<Listener> prev = new ArrayList<>(this.attachedListeners.getOrDefault(l, List.of()));
            if(!prev.contains(listener)){
                prev.add(listener);
            }
            this.attachedListeners.put(l, prev);
        }
    }

    /**
     * This method removes {@param listener} from updates list of type {@param type}
     * @param type the {@link ListenerType} from which unregister {@param listener}
     * @param listener the {@link Listener} to unregister
     */
    public void removeListener(ListenerType type, Listener listener){
        this.attachedListeners.get(type).remove(listener);
    }

    /**
     * This method removes {@param listener} from all updates list in which it was registered
     * @param listener the {@link Listener} to unregister
     */
    public void removeListener(Listener listener){
        for(ListenerType l : this.attachedListeners.keySet()){
            this.attachedListeners.get(l).remove(listener);
        }
    }

    /**
     * This method is used to notify {@link ChatListener}
     * @param messages the ArrayList of Messages inside chat
     */
    public void notifyChatListener(ArrayList<Message> messages){
        for(Listener c : this.attachedListeners.get(ListenerType.CHAT_LISTENER)) {
            ((ChatListener) c).notify(messages);
        }
    }

    /**
     * This method is used to notify {@link LocalModelListener}.
     * @param type the {@link LocalModelEvents} that happened
     * @param localModel the {@link LocalModel} on which the event happened
     * @param varArgs variable {@link String} arguments
     */
    public void notifyLocalModelListener(LocalModelEvents type, LocalModel localModel, String ... varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.LOCAL_MODEL_LISTENER)) {
            ((LocalModelListener) c).notify(type, localModel, varArgs);
        }
    }

    /**
     * This method is used to notify {@link StationListener} about an event
     * concerning {@link PersonalStation}
     * @param otherStation the {@link PersonalStation} on which the event happened
     */
    public void notifyStationListener(PersonalStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notify(otherStation);
        }
    }

    /**
     * This method is used to notify {@link StationListener} about an event
     * concerning {@link OtherStation}
     * @param otherStation the {@link OtherStation} on which the event happened
     */
    public void notifyStationListener(OtherStation otherStation){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notify(otherStation);
        }
    }

    /**
     * This method is used to notify {@link StationListener} about errors
     * @param args variable {@link String} arguments describing the error
     */
    public void notifyErrorStationListener(String ... args){
        for(Listener c : this.attachedListeners.get(ListenerType.STATION_LISTENER)) {
            ((StationListener) c).notifyErrorStation(args);
        }
    }

    /**
     * This method is used to notify {@link SetupListener} about an event
     * concerning setup phase.
     * @param type is the {@link SetupEvent} of the event
     */
    public void notifySetupListener(SetupEvent type){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(type);
        }
    }

    /**
     * This method is used to notify {@link SetupListener} about errors.
     * @param error a {@link String} description of the error
     */
    public void notifyErrorSetupListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.SETUP_LISTENER)) {
            ((SetupListener) c).notify(error);
        }
    }

    /**
     * This method is used to notify {@link TableListener} about an event
     * concerning table.
     * @param localTable the {@link LocalTable} on which the event happened
     */
    public void notifyTableListener(LocalTable localTable){
        for(Listener c : this.attachedListeners.get(ListenerType.TABLE_LISTENER)) {
            ((TableListener) c).notify(localTable);
        }
    }

    /**
     * This method is used to notify {@link TurnStateListener} about an event
     * concerning turn state.
     * @param nick is the nickname of the player who is going to play
     * @param turnState is the {@link TurnState} of player
     */
    public void notifyTurnStateListener(String nick, TurnState turnState){
        for(Listener c : this.attachedListeners.get(ListenerType.TURN_LISTENER)) {
            ((TurnStateListener) c).notify(nick, turnState);
        }
    }

    /**
     * This method is used to notify {@link GameHandlingListener} that an event concerning
     * game handling has happened.
     * @param type a {@link GameHandlingEvents} describing the type of event
     * @param varArgs variable {@link String} arguments describing the event
     */
    public void notifyGameHandlingListener(GameHandlingEvents type, List<String> varArgs){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)) {
            ((GameHandlingListener) c).notify(type, varArgs);
        }
    }

    /**
     * This method is used to notify {@link GameHandlingListener} that an error has occurred
     * @param errorDescription a {@link String} description of the error
     */
    public void notifyErrorGameHandlingListener(String errorDescription){
        for(Listener c : this.attachedListeners.get(ListenerType.GAME_HANDLING_EVENTS_LISTENER)) {
            ((GameHandlingListener) c).notify(errorDescription);
        }
    }

    /**
     * This method is used to notify {@link PlayerCreationListener} that player has
     * been correctly created
     * @param name is he name of the player
     */
    public void notifyPlayerCreationListener(String name){
        for(Listener c : this.attachedListeners.get(ListenerType.PLAYER_CREATION_LISTENER)) {
            ((PlayerCreationListener) c).notifyPlayerCreation(name);
        }
    }

    /**
     * This method is used to notify {@link PlayerCreationListener} that an error has occurred.
     * @param error a {@link String} describing the error
     */
    public void notifyErrorPlayerCreationListener(String error){
        for(Listener c : this.attachedListeners.get(ListenerType.PLAYER_CREATION_LISTENER)) {
            ((PlayerCreationListener) c).notifyPlayerCreationError(error);
        }
    }

    /**
     * This method is used to notify {@link StateListener} that an event regarding
     * {@link ViewState} has happened
     * @param viewState the new {@link ViewState}
     */
    public void notifyStateListener(ViewState viewState){
        if(this.attachedListeners.get(ListenerType.STATE_LISTENER) != null) {
            for (Listener l : this.attachedListeners.get(ListenerType.STATE_LISTENER)) {
                ((StateListener) l).notify(viewState);
            }
        }
    }

}