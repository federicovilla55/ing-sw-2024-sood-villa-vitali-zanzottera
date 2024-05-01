package it.polimi.ingsw.gc19.View.GameLocalView;

public enum Command {
    CREATEPLAYER("create_player", 1),
    CREATEGAME("create_game", 2),

    CREATEGAMESEED("create_game_seed", 3),

    JOINGAME("join_game", 1),

    JOINFIRSTGAME("join_first_game", 0),

    AVAILABLEGAMES("available_games", 0),

    PLACECARD("place_card", 4),

    SENDCHATMESSAGE("send_message", 2),

    PLACEINITIALCARD("place_initial_card",  1),

    PICKCARDTABLE("pick_card_table", 2),

    PICKCARDDECK("pick_card_deck", 1),

    CHOOSECOLOR("choose_color", 1),

    CHOOSEPRIVATEGOAL("choose_goal", 1);

    private final String commandName;
    private final int numArgs;
    Command(String commandName, int numArgs){
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
