package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.List;
import java.util.Map;

public class OwnStationConfigurationMessage extends ConfigurationMessage {

    private final String nick;
    private final Color color;
    private final List<PlayableCard> cardsInHand;
    private final Map<Symbol, Integer> visibleSymbols;
    private final GoalCard privateGoalCard;
    private final int numPoints;
    private final PlayableCard initialCard;
    private final GoalCard goalCard1;
    private final GoalCard goalCard2;
    private final List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence;

    public OwnStationConfigurationMessage(String nick, Color color, List<PlayableCard> cardsInHand, Map<Symbol, Integer> visibleSymbols, GoalCard privateGoalCard, int numPoints,
                                          PlayableCard initialCard, GoalCard goalCard1, GoalCard goalCard2, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.nick = nick;
        this.color = color;
        this.cardsInHand = cardsInHand;
        this.visibleSymbols = visibleSymbols;
        this.privateGoalCard = privateGoalCard;
        this.numPoints = numPoints;
        this.initialCard = initialCard;
        this.goalCard1 = goalCard1;
        this.goalCard2 = goalCard2;
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

    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

    public GoalCard getGoalCard1() {
        return this.goalCard1;
    }

    public GoalCard getGoalCard2() {
        return this.goalCard2;
    }

    public Color getColor() {
        return color;
    }

    public List<PlayableCard> getCardsInHand() {
        return cardsInHand;
    }

    public GoalCard getPrivateGoalCard() {
        return privateGoalCard;
    }

    public List<Tuple<PlayableCard, Tuple<Integer, Integer>>> getPlacedCardSequence() {
        return placedCardSequence;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
