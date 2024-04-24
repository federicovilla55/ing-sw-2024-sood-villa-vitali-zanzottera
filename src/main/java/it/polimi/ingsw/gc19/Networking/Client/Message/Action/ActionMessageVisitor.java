package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

/**
 * This interface is used for implementing Visitor design pattern
 */
public interface ActionMessageVisitor{

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link ChosenGoalCardMessage}
     * @param message the {@link ChosenGoalCardMessage} to visit
     */
    void visit(ChosenGoalCardMessage message);

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link DirectionOfInitialCardMessage}
     * @param message the {@link DirectionOfInitialCardMessage} to visit
     */
    void visit(DirectionOfInitialCardMessage message);

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link PlaceCardMessage}
     * @param message the {@link PlaceCardMessage} to visit
     */
    void visit(PlaceCardMessage message);

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link ChosenColorMessage}
     * @param message the {@link ChosenColorMessage} to visit
     */
    void visit(ChosenColorMessage message);

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link PickCardFromDeckMessage}
     * @param message the {@link PickCardFromDeckMessage} to visit
     */
    void visit(PickCardFromDeckMessage message);

    /**
     * This method is used by {@link ActionMessageVisitor} to visit
     * a message {@link PickCardFromTableMessage}
     * @param message the {@link PickCardFromTableMessage} to visit
     */
    void visit(PickCardFromTableMessage message);
}
