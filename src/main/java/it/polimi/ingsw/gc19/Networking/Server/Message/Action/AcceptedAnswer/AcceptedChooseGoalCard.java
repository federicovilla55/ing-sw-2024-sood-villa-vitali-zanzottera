package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Model.Card.GoalCard;
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
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
