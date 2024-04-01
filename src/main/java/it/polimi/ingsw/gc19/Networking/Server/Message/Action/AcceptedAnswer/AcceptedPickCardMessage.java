package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

import java.util.ArrayList;

public class AcceptedPickCardMessage extends AcceptedActionMessage{

    private boolean isFromTable;
    private Integer position;

    private final PlayableCard pickedCard;
    private final Symbol symbol;

    public AcceptedPickCardMessage(PlayableCard pickedCard, Symbol symbol){
        super();
        this.pickedCard = pickedCard;
        this.symbol = symbol;
        this.isFromTable = false;
        this.position = null;
    }

    public AcceptedPickCardMessage(PlayableCard pickedCard, Symbol symbol, boolean isFromTable, int position){
        this(pickedCard, symbol);
        this.isFromTable = isFromTable;
        this.position = position;
    }

    public boolean isFromTable() {
        return this.isFromTable;
    }

    public Integer getPosition() {
        return this.position;
    }

    public PlayableCard getPickedCard() {
        return this.pickedCard;
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

}
