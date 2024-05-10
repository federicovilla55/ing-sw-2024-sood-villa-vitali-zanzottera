package it.polimi.ingsw.gc19.View.Command;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;

public enum CommandType {
    CREATE_PLAYER("create_player", 1),
    CREATE_GAME("create_game", 2),
    JOIN_GAME("join_game", 1),
    JOIN_FIRST_GAME("join_first_game", 0),
    AVAILABLE_GAMES("available_games", 0),
    PLACE_CARD("place_card", 4),
    SEND_CHAT_MESSAGE("send_message", 2),
    PLACE_INITIAL_CARD("place_inaitial_card", 1),
    PICK_CARD_TABLE("pick_card_table", 2),
    PICK_CARD_DECK("pick_card_deck", 1),
    CHOOSE_COLOR("choose_color", 1),
    CHOOSE_PRIVATE_GOAL_CARD("choose_goal", 1),
    LOGOUT_FROM_GAME("logout_from_game", 0),
    DISCONNECT("disconnect", 0),
    SHOW_STATION("show_station", 1),
    SHOW_PERSONAL_STATION("show_personal_station", 0),
    SHOW_CHAT("show_chat", 0),
    AVAILABLE_COLORS("available_colors", 0),
    SHOW_PRIVATE_GOAL_CARD("show_private_goal_card", 0),
    SHOW_INITIAL_CARD("show_initial_card", 0);

    private final String commandName;

    private final int numArgs;

    CommandType(String commandName, int numArgs){
        this.commandName = commandName;
        this.numArgs = numArgs;
    }

    public String getCommandName(){
        return this.commandName;
    }

    public int getNumArgs() {
        return this.numArgs;
    }

}
