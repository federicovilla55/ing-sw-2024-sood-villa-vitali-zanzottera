package it.polimi.ingsw.gc19.Model.Deck;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

class DeckTest {

    @Test
    void init() throws IOException {
        Deck<PlayableCard> d = new Deck<PlayableCard>("r.json");

        ArrayList<PlayableCard> a = d.init("r.json");

        System.out.println(a.get(0));

    }
}