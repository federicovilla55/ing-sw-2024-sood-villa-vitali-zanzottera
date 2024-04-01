package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;

import java.util.HashMap;

public class AcceptedPlaceInitialCard extends AcceptedActionMessage{

    private final HashMap<Symbol, Integer> visibleSymbol;
    private final PlayableCard initialCard;

    public AcceptedPlaceInitialCard(PlayableCard initialCard, HashMap<Symbol, Integer> visibleSymbol){
        this.initialCard = initialCard;
        this.visibleSymbol = visibleSymbol;
    }

    public HashMap<Symbol, Integer> getVisibleSymbol() {
        return this.visibleSymbol;
    }

    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

}
