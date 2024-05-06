package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

public class LocalTable {
    private PlayableCard resource1;
    private PlayableCard resource2;
    private PlayableCard gold1;
    private PlayableCard gold2;
    private GoalCard publicGoal1;
    private GoalCard publicGoal2;
    private Symbol nextSeedOfResourceDeck;
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

    public PlayableCard getResource1() {
        return resource1;
    }

    public PlayableCard getResource2() {
        return resource2;
    }

    public PlayableCard getGold1() {
        return gold1;
    }

    public PlayableCard getGold2() {
        return gold2;
    }

    public GoalCard getPublicGoal1() {
        return publicGoal1;
    }

    public GoalCard getPublicGoal2() {
        return publicGoal2;
    }

    public void setGold1(PlayableCard gold1) {
        this.gold1 = gold1;
    }

    public void setGold2(PlayableCard gold2) {
        this.gold2 = gold2;
    }

    public void setResource1(PlayableCard resource1) {
        this.resource1 = resource1;
    }

    public void setResource2(PlayableCard resource2) {
        this.resource2 = resource2;
    }

    public Symbol getNextSeedOfResourceDeck() {
        return nextSeedOfResourceDeck;
    }

    public Symbol getNextSeedOfGoldDeck() {
        return nextSeedOfGoldDeck;
    }
}