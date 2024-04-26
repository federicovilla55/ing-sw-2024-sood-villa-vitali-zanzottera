package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * Classes that need to visit {@link ConfigurationMessage} must
 * implement this interface
 */
public interface ConfigurationMessageVisitor{

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link ConfigurationMessage}
     * @param message the {@link ConfigurationMessage} to visit
     */
    void visit(ConfigurationMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link GameConfigurationMessage}
     * @param message the {@link GameConfigurationMessage} to visit
     */
    void visit(GameConfigurationMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link OtherStationConfigurationMessage}
     * @param message the {@link OtherStationConfigurationMessage} to visit
     */
    void visit(OtherStationConfigurationMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link OwnStationConfigurationMessage}
     * @param message the {@link OwnStationConfigurationMessage} to visit
     */
    void visit(OwnStationConfigurationMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link TableConfigurationMessage}
     * @param message the {@link TableConfigurationMessage} to visit
     */
    void visit(TableConfigurationMessage message);

}
