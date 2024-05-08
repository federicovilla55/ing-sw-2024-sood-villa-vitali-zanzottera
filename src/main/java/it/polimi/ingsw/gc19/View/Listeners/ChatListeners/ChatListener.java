package it.polimi.ingsw.gc19.View.Listeners.ChatListeners;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

import java.util.ArrayList;

public interface ChatListener extends Listener {

    void notify(ArrayList<Message> msg);

}
