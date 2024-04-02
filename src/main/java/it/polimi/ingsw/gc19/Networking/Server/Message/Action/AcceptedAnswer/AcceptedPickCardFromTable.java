package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;

public class AcceptedPickCardFromTable extends AcceptedPickCardMessage{

    private final Tuple<Integer, Integer> coords;

    public AcceptedPickCardFromTable(String nick, PlayableCard pickedCard, Symbol symbol, Tuple<Integer, Integer> coords) {
        super(nick, pickedCard, symbol);
        this.coords = coords;
    }

    public Tuple<Integer, Integer> getCoords(){
        return this.coords;
    }

}
