package it.polimi.ingsw.gc19.View.Listeners.SetupEventsListeners;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;

public interface SetupEventsListener{

    void notifyColorAccepted(Color color);

    void notifyInitialCard(CardOrientation cardOrientation);

    void notifyGoalCard(GoalCard goalCard);

}
