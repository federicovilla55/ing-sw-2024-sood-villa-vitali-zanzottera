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
    private  GoalCard publicGoal2;
    private Symbol nextSeedOfResourceDeck;
    private Symbol nextSeedOfGoldDeck;

    LocalTable(PlayableCard resource1, PlayableCard resource2, PlayableCard gold1, PlayableCard gold2,
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
}