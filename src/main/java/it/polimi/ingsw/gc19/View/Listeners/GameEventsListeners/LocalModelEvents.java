package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;

/**
 * This enum represents the possible events regarding {@link LocalModel}
 * (e.g. new player connected to game or disconnected).
 */
public enum LocalModelEvents {
    NEW_PLAYER_CONNECTED,
    DISCONNECTED_PLAYER,
    RECONNECTED_PLAYER

}