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
    PLACE_INITIAL_CARD("place_initial_card", 1),
    PICK_CARD_TABLE("pick_card_table", 2),
    PICK_CARD_DECK("pick_card_deck", 1),
    CHOOSE_COLOR("choose_color", 1),
    CHOOSE_PRIVATE_GOAL_CARD("choose_goal", 1);

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
