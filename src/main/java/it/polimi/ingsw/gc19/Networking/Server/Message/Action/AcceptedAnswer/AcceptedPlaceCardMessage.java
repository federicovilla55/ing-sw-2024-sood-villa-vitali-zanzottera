package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;

import java.util.Map;

/**
 * This abstract class represent a generic message sent by server
 * to accept placing a card (initial or playable)
 */
public abstract class AcceptedPlaceCardMessage extends AcceptedActionMessage{

    private final String nick;
    private final Map<Symbol, Integer> visibleSymbols;

    protected AcceptedPlaceCardMessage(String nick, Map<Symbol, Integer> visibleSymbols){
        super();
        this.nick = nick;
        this.visibleSymbols = visibleSymbols;
    }

    /**
     * Getter for new hashmap of visible symbols after placing card
     * @return updated hashmap of visible symbols
     */
    public Map<Symbol, Integer> getVisibleSymbols(){
        return this.visibleSymbols;
    }

    /**
     * Getter for nickname of player who requested to place a card
     * @return the nick of player who requested to place card
     */
    public String getNick() {
        return this.nick;
    }

}
