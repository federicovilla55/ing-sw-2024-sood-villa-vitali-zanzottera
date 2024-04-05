package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.List;
import java.util.Map;

public class OtherStationConfigurationMessage extends ConfigurationMessage {

    private final String nick;
    private final Color color;
    private final Map<Symbol, Integer> visibleSymbols;
    private final int numPoints;
    private final List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    public OtherStationConfigurationMessage(String nick, Color color, Map<Symbol, Integer> visibleSymbols, int numPoints, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.nick = nick;
        this.color = color;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.placedCardSequence = placedCardSequence;
    }

    public String getNick() {
        return this.nick;
    }

    public Map<Symbol, Integer> getVisibleSymbols() {
        return this.visibleSymbols;
    }

    public int getNumPoints() {
        return this.numPoints;
    }

    public Color getColor() {
        return color;
    }

    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        return placedCardSequence;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}