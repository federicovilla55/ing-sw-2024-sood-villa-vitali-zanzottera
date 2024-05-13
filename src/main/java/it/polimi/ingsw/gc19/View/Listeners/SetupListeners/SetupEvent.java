package it.polimi.ingsw.gc19.View.Listeners.SetupListeners;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
/**
 * This enum represents all possible updates type that can
 * be received while in {@link ViewState#SETUP}
 */
public enum SetupEvent {
    AVAILABLE_COLOR,
    ACCEPTED_COLOR,
    ACCEPTED_PRIVATE_GOAL_CARD,
    ACCEPTED_INITIAL_CARD,
    COMPLETED
}