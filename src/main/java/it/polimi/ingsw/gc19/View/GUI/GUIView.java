package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.NotPlayer;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.NewConfigurationController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class GUIView extends Application {

    private LocalModel localModel;
    private  CommandParser commandParser;
    private  ClientController clientController;
    private  SceneStatesConst scenePath;
    @Override
    public void start(Stage stage) throws Exception {
        Configuration config;
        Configuration.ConnectionType connectionType;
        String reconnectChoice;
        ClientInterface client;
        Parent root;
        this.scenePath = new SceneStatesConst();
        this.commandParser = new CommandParser(new ClientController());
        this.clientController = commandParser.clientController();
        try {
            config = ConfigurationManager.retriveConfiguration();
            connectionType = config.getConnectionType();
            File url = new File(this.scenePath.OldConfigurationScene);
            root = FXMLLoader.load(url.toURL());

        } catch (RuntimeException e) {
            File url = new File(this.scenePath.NewConfigurationScene);
            FXMLLoader loader = new FXMLLoader(url.toURL());
            root = loader.load();//FXMLLoader.load(url.toURL());
            NewConfigurationController controller = loader.getController();
            controller.setCommandParser(this.commandParser);
            controller.setClientController(this.clientController);
            controller.setScenePath(this.scenePath);
        }
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
