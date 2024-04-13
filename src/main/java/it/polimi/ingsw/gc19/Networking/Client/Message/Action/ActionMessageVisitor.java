package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

public interface ActionMessageVisitor{
    void visit(ChosenGoalCardMessage message);
    void visit(DirectionOfInitialCardMessage message);
    void visit(PlaceCardMessage message);
    void visit(ChosenColorMessage message);
    void visit(PickCardFromDeckMessage message);
    void visit(PickCardFromTableMessage message);
}
