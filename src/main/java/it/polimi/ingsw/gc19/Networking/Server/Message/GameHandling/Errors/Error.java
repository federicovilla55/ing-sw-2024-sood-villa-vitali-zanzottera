package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors;

/**
 * This enum represents all possible errors concerning
 * game and player management (such as duplicate nickname
 * or invalid game name).
 */
public enum Error{
    PLAYER_NAME_ALREADY_IN_USE,
    GAME_NAME_ALREADY_IN_USE,
    PLAYER_NOT_IN_GAME,
    GAME_NOT_FOUND,
    GAME_NOT_ACCESSIBLE,
    PLAYER_ALREADY_REGISTERED_TO_SOME_GAME,
    NO_GAMES_FREE_TO_JOIN,
    CANNOT_BUILD_GAME,
    INCORRECT_NUMBER_OF_PLAYERS
}