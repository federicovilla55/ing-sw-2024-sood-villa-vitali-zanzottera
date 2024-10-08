package it.polimi.ingsw.gc19.View.Command;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;

/**
 * This enum represents all the commands that have to be processed by
 * {@link CommandParser} first and then {@link ClientController}.
 */
public enum CommandType {
    CREATE_PLAYER("create_player", 1),
    CREATE_GAME("create_game", 2),
    JOIN_GAME("join_game", 1),
    JOIN_FIRST_GAME("join_first_game", 0),
    AVAILABLE_GAMES("available_games", 0),
    PLACE_CARD("place_card", 4),
    SEND_CHAT_MESSAGE("send_chat_message", 2),
    PLACE_INITIAL_CARD("place_initial_card", 1),
    PICK_CARD_TABLE("pick_card_table", 1),
    PICK_CARD_DECK("pick_card_deck", 1),
    CHOOSE_COLOR("choose_color", 1),
    CHOOSE_GOAL("choose_goal", 1),
    LOGOUT_FROM_GAME("logout_from_game", 0),
    DISCONNECT("disconnect", 0),
    AVAILABLE_COLORS("available_colors", 0);

    /**
     * The name of the command
     */
    private final String commandName;

    /**
     * umber of args of the command
     */
    private final int numArgs;

    CommandType(String commandName, int numArgs){
        this.commandName = commandName;
        this.numArgs = numArgs;
    }

    /**
     * Getter for command name
     * @return the command name
     */
    public String getCommandName(){
        return this.commandName;
    }

    /**
     * Getter for umber of arguments of the command
     * @return the number of arguments of the command
     */
    public int getNumArgs() {
        return this.numArgs;
    }

}