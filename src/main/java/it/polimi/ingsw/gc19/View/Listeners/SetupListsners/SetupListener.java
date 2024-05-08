package it.polimi.ingsw.gc19.View.Listeners.SetupListsners;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface SetupListener extends Listener {

    void notify(Color color);

    void notify(GoalCard goalCard);

    void notify(CardOrientation cardOrientation);

}
