package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

/**
 * The class contains the table that the Client can access to pick card or be represented by its view.
 * It is created by the {@link LocalModel} class.
 */
public class LocalTable {
    /**
     * The first resource card on the table.
     */
    private PlayableCard resource1;
    /**
     * The second resource card on the table.
     */
    private PlayableCard resource2;
    /**
     * The first gold card on the table.
     */
    private PlayableCard gold1;
    /**
     * The second gold card on the table.
     */
    private PlayableCard gold2;
    /**
     * The first public goal card on the table.
     */
    private final GoalCard publicGoal1;

    /**
     * The second public goal card on the table.
     */
    private final GoalCard publicGoal2;

    /**
     * The attribute contains the {@link Symbol} of the next card that will be picked from the resource deck.
     */
    private Symbol nextSeedOfResourceDeck;

    /**
     * The attribute contains the {@link Symbol} of the next card that will be picked from the gold deck.
     */
    private Symbol nextSeedOfGoldDeck;

    public LocalTable(PlayableCard resource1, PlayableCard resource2, PlayableCard gold1, PlayableCard gold2,
              GoalCard publicGoal1, GoalCard publicGoal2, Symbol nextSeedOfResourceDeck, Symbol nextSeedOfGoldDeck){
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
     * To return
     * @return the first resource card on the table
     */
    public PlayableCard getResource1() {
        return resource1;
    }

    /**
     * To return
     * @return the second resource card on the table
     */
    public PlayableCard getResource2() {
        return resource2;
    }

    /**
     * To return
     * @return the first gold card on the table
     */
    public PlayableCard getGold1() {
        return gold1;
    }

    /**
     * To return
     * @return the second gold card on the table
     */
    public PlayableCard getGold2() {
        return gold2;
    }

    /**
     * To return
     * @return the first public Goal card on the table
     */
    public GoalCard getPublicGoal1() {
        return publicGoal1;
    }

    /**
     * To return
     * @return the second Public Goal card on the table
     */
    public GoalCard getPublicGoal2() {
        return publicGoal2;
    }

    /**
     * To set the first Gold card that will be shown in the table.
     * @param gold1, the first Gold card that will be available to
     *               pick in the table.
     */
    public void setGold1(PlayableCard gold1) {
        this.gold1 = gold1;
    }

    /**
     * To set the second Gold card that will be shown in the table.
     * @param gold2, the second Gold card that will be available to
     *               pick in the table.
     */
    public void setGold2(PlayableCard gold2) {
        this.gold2 = gold2;
    }

    /**
     * To set the first Resource card that will be shown in the table.
     * @param resource1, the first Resource card that will be available to
     *               pick in the table.
     */
    public void setResource1(PlayableCard resource1) {
        this.resource1 = resource1;
    }

    /**
     * To set the second Resource card that will be shown in the table.
     * @param resource2, the second Resource card that will be available to
     *               pick in the table.
     */
    public void setResource2(PlayableCard resource2) {
        this.resource2 = resource2;
    }

    /**
     * To return
     * @return the {@link Symbol} of the next card that will be picked from the Resource deck
     */
    public Symbol getNextSeedOfResourceDeck() {
        return nextSeedOfResourceDeck;
    }

    /**
     * To return
     * @return the {@link Symbol} of the next card that will be picked from the Gold deck
     */
    public Symbol getNextSeedOfGoldDeck() {
        return nextSeedOfGoldDeck;
    }

    /**
     * To set the {@link Symbol} of the next card that will be picked from the Gold deck.
     * @param nextSeedOfGoldDeck, the {@link Symbol} that will be picked next from the Gold deck.
     */
    public void setNextSeedOfGoldDeck(Symbol nextSeedOfGoldDeck) {
        this.nextSeedOfGoldDeck = nextSeedOfGoldDeck;
    }


    /**
     * To set the {@link Symbol} of the next card that will be picked from the Resource deck.
     * @param nextSeedOfResourceDeck, the {@link Symbol} that will be picked next from the Resource deck.
     */
    public void setNextSeedOfResourceDeck(Symbol nextSeedOfResourceDeck) {
        this.nextSeedOfResourceDeck = nextSeedOfResourceDeck;
    }
}