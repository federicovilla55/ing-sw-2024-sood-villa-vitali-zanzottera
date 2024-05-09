package it.polimi.ingsw.gc19.View.Listeners.SetupListsners;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

import java.util.List;

public interface SetupListener extends Listener {

    void notify(Color color);

    void notify(GoalCard goalCard);

    void notify(PlayableCard initialCard);

    void notify(List<Color> colors);

    void notify(GoalCard goalCard1, GoalCard goalCard2);

}
