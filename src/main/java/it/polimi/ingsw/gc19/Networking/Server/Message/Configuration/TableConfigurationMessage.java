package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class TableConfigurationMessage extends ConfigurationMessage {

    /**
     * First resource card on table
     */
    private final PlayableCard resource1;

    /**
     * Second resource card on table
     */
    private final PlayableCard resource2;

    /**
     * First gold card on table
     */
    private final PlayableCard gold1;

    /**
     * Second gold card on table
     */
    private final PlayableCard gold2;

    /**
     * First public goal card on table
     */
    private final GoalCard publicGoal1;

    /**
     * Second public goal card on table
     */
    private final GoalCard publicGoal2;

    /**
     * Seed of card on top of resource deck
     *
     */
    private final Symbol nextSeedOfResourceDeck;

    /**
     * Seed of card on top of gold deck
     */
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

    /**
     * Getter for {@link #resource1}
     * @return {@link #resource1}
     */
    public PlayableCard getSxResource(){
        return resource1;
    }

    /**
     * Getter for {@link #resource2}
     * @return {@link #resource2}
     */
    public PlayableCard getDxResource(){
        return resource2;
    }

    /**
     * Getter for {@link #gold1}
     * @return {@link #gold1}
     */
    public PlayableCard getSxGold(){
        return gold1;
    }

    /**
     * Getter for {@link #gold2}
     * @return {@link #gold2}
     */
    public PlayableCard getDxGold(){
        return gold2;
    }

    /**
     * Getter for {@link #publicGoal1}
     * @return {@link #publicGoal1}
     */
    public GoalCard getSxPublicGoal(){
        return publicGoal1;
    }

    /**
     * Getter for {@link #publicGoal2}
     * @return {@link #publicGoal2}
     */
    public GoalCard getDxPublicGoal(){
        return publicGoal2;
    }

    /**
     * Getter for {@link #nextSeedOfResourceDeck}
     * @return {@link #nextSeedOfResourceDeck}
     */
    public Symbol getNextSeedOfResourceDeck(){
        return nextSeedOfResourceDeck;
    }

    /**
     * Getter for {@link #nextSeedOfGoldDeck}
     * @return {@link #nextSeedOfGoldDeck}
     */
    public Symbol getNextSeedOfGoldDeck(){
        return nextSeedOfGoldDeck;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof ConfigurationMessageVisitor) ((ConfigurationMessageVisitor) visitor).visit(this);
    }

}