package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class AcceptedChooseGoalCard extends AcceptedActionMessage{

    private final String goalCardCode;

    public AcceptedChooseGoalCard(String goalCardCode){
        this.goalCardCode = goalCardCode;
    }

    public String getGoalCardCode() {
        return this.goalCardCode;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
