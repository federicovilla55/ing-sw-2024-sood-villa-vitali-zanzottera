package it.polimi.ingsw.gc19.View.Listeners.ChatListener;

import it.polimi.ingsw.gc19.Model.Chat.Message;

import java.util.ArrayList;

public interface ChatEventsListener{

    void notifyEvent(ArrayList<Message> messages);

}
