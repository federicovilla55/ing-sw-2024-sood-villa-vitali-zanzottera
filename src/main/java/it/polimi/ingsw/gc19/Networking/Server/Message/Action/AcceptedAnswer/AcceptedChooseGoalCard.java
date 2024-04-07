package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class AcceptedChooseGoalCard extends AcceptedActionMessage{

    private final GoalCard goalCard;

    public AcceptedChooseGoalCard(GoalCard goalCard){
        this.goalCard = goalCard;
    }
    public GoalCard getGoalCard() {
        return this.goalCard;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }


}
