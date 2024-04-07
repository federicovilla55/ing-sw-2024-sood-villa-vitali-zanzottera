package it.polimi.ingsw.gc19.Networking.Server.Message.Action;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;

public interface AnswerToActionMessageVisitor {
    void visit(AcceptedChooseGoalCard message);
    void visit(AcceptedColorMessage message);
    void visit(OwnAcceptedPickCardFromDeckMessage message);
    void visit(OtherAcceptedPickCardFromDeckMessage message);
    void visit(AcceptedPickCardFromTable message);
    void visit(AcceptedPlaceCardMessage message);
    void visit(AcceptedPlaceInitialCard message);
    void visit(RefusedActionMessage visitor);

}
