package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class TableConfigurationMessage extends ConfigurationMessage {

    private final PlayableCard resource1;
    private final PlayableCard resource2;
    private final PlayableCard gold1;
    private final PlayableCard gold2;
    private final GoalCard publicGoal1;
    private final GoalCard publicGoal2;
    private final Symbol nextSeedOfResourceDeck;
    private final Symbol nextSeedOfGoldDeck;

    public TableConfigurationMessage(PlayableCard resource1, PlayableCard resource2,
                                     PlayableCard gold1, PlayableCard gold2,
                                     GoalCard publicGoal1, GoalCard publicGoal2,
                                     Symbol nextSeedOfResourceDeck, Symbol nextSeedOfGoldDeck){
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.gold1 = gold1;
        this.gold2 = gold2;
        this.publicGoal1 = publicGoal1;
        this.publicGoal2 = publicGoal2;
        this.nextSeedOfResourceDeck = nextSeedOfResourceDeck;
        this.nextSeedOfGoldDeck = nextSeedOfGoldDeck;
    }

    public PlayableCard getSxResource(){
        return resource1;
    }

    public PlayableCard getDxResource(){
        return resource2;
    }

    public PlayableCard getSxGold(){
        return gold1;
    }

    public PlayableCard getDxGold(){
        return gold2;
    }

    public GoalCard getSxPublicGoal(){
        return publicGoal1;
    }

    public GoalCard getDxPublicGoal(){
        return publicGoal2;
    }

    public Symbol getNextSeedOfResourceDeck(){
        return nextSeedOfResourceDeck;
    }

    public Symbol getNextSeedOfGoldDeck(){
        return nextSeedOfGoldDeck;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
