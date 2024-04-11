package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to notify player that that his
 * choice for private goal card has been accepted.
 */
public class AcceptedChooseGoalCard extends AcceptedActionMessage{

    private final GoalCard goalCard;

    public AcceptedChooseGoalCard(GoalCard goalCard){
        this.goalCard = goalCard;
    }

    /**
     * This method return the private goal card chosen
     * @return private goal gard chosen
     */
    public GoalCard getGoalCard() {
        return this.goalCard;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}
