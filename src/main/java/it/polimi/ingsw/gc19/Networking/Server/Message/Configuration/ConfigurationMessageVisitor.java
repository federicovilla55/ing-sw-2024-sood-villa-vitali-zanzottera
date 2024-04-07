package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public interface ConfigurationMessageVisitor{

    void visit(ConfigurationMessage message);
    void visit(GameConfigurationMessage message);
    void visit(OtherStationConfigurationMessage message);
    void visit(OwnStationConfigurationMessage message);
    void visit(TableConfigurationMessage message);

}
