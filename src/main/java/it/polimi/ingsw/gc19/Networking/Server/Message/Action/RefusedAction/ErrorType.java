package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

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
    GENERIC;
}
