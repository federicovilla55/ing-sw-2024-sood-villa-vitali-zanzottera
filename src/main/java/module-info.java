module it.polimi.ingsw.gc19 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.rmi;

    opens it.polimi.ingsw.gc19 to javafx.fxml;
    exports it.polimi.ingsw.gc19;
    exports it.polimi.ingsw.gc19.Model.Card to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.gc19.Enums to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Enums to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Model.Card;
    opens it.polimi.ingsw.gc19.Model;
    exports it.polimi.ingsw.gc19.Costants to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Costants;
    exports it.polimi.ingsw.gc19.Model.Game to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Model.Game;
    exports it.polimi.ingsw.gc19.Model.Deck to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Model.Deck;
    exports it.polimi.ingsw.gc19.Model;
    exports it.polimi.ingsw.gc19.Networking.Server.Message.Action;
    opens it.polimi.ingsw.gc19.Networking.Server.Message.Action;
    exports it.polimi.ingsw.gc19.Networking.Server.Message;
    exports it.polimi.ingsw.gc19.Controller;
    opens it.polimi.ingsw.gc19.Controller;
    exports it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;
    opens it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;
    exports it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;
    opens it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;
    exports it.polimi.ingsw.gc19.ObserverPattern;
    exports it.polimi.ingsw.gc19.Model.Station;
    exports it.polimi.ingsw.gc19.Model.Chat;
    exports it.polimi.ingsw.gc19.Networking.Server;
    exports it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;
    opens it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;
    exports it.polimi.ingsw.gc19.Networking.Client;
    exports it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;
    opens it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;
}