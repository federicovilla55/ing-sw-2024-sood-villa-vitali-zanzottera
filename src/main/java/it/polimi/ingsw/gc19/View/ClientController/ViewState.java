package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;

/**
 * This enum represents all possible states of FSM describing {@link ClientController}
 */
public enum ViewState{

    NOT_PLAYER,
    NOT_GAME,
    SETUP,
    PLACE,
    PICK,
    OTHER_TURN,
    PAUSE,
    DISCONNECT,
    END,
    WAIT

}