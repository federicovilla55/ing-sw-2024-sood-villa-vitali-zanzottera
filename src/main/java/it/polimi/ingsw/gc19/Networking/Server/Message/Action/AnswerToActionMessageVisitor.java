package it.polimi.ingsw.gc19.Networking.Server.Message.Action;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;

/**
 * Classes that need to visit {@link AnswerToActionMessage}
 * must be implemented this interface.
 */
public interface AnswerToActionMessageVisitor {

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link AcceptedChooseGoalCard}
     * @param message the {@link AcceptedChooseGoalCard} to visit
     */
    void visit(AcceptedChooseGoalCard message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link AcceptedColorMessage}
     * @param message the {@link AcceptedColorMessage} to visit
     */
    void visit(AcceptedColorMessage message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link OwnAcceptedPickCardFromDeckMessage}
     * @param message the {@link OwnAcceptedPickCardFromDeckMessage} to visit
     */
    void visit(OwnAcceptedPickCardFromDeckMessage message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link OtherAcceptedPickCardFromDeckMessage}
     * @param message the {@link OtherAcceptedPickCardFromDeckMessage} to visit
     */
    void visit(OtherAcceptedPickCardFromDeckMessage message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link AcceptedPickCardFromTable}
     * @param message the {@link AcceptedPickCardFromTable} to visit
     */
    void visit(AcceptedPickCardFromTable message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link AcceptedPlacePlayableCardMessage}
     * @param message the {@link AcceptedPlacePlayableCardMessage} to visit
     */
    void visit(AcceptedPlacePlayableCardMessage message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link AcceptedPlaceInitialCard}
     * @param message the {@link AcceptedPlaceInitialCard} to visit
     */
    void visit(AcceptedPlaceInitialCard message);

    /**
     * This method is used by {@link AnswerToActionMessageVisitor} to visit
     * a message {@link RefusedActionMessage}
     * @param message the {@link RefusedActionMessage} to visit
     */
    void visit(RefusedActionMessage message);

}
