package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

import java.util.ArrayList;

/**
 * Classes that implement this interface are chat listeners (e.g.
 * they are going to receive updates about chat)
 */
public interface ChatListener extends Listener {

    /**
     * This method is used to notify listeners about chat events
     * @param msg the ArrayList of Messages stored in chat
     */
    void notify(ArrayList<Message> msg);

}