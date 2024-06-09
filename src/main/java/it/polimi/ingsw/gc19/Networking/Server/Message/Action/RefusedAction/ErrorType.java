package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

import it.polimi.ingsw.gc19.Enums.GameState;

/**
 * This enum represents all possible errors that can occur
 * during {@link GameState#PLAYING}, {@link GameState#END}
 * and {@link GameState#SETUP}
 */
public enum ErrorType{
    INVALID_ANCHOR_ERROR,
    INVALID_CARD_ERROR,
    EMPTY_DECK,
    EMPTY_TABLE_SLOT,
    INVALID_GOAL_CARD_ERROR,
    INVALID_GAME_STATE,
    INVALID_TURN_STATE,
    COLOR_ALREADY_CHOSEN,
    GOAL_CARD_ALREADY_CHOSEN,
    GENERIC,
    NOT_YOUR_TURN;
}