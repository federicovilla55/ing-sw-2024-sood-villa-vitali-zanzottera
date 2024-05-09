package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.*;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

public interface GeneralListener extends Listener,
                                         ChatListener,
                                         LocalModelListener,
                                         StationListener,
                                         TableListener,
                                         TurnStateListener,
                                         GameHandlingListener,
                                         PlayerCreationListener,
                                         SetupListener,
                                         StateListener {

}