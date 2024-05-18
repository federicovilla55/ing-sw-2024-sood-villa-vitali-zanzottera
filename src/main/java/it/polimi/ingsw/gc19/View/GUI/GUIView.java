package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.NewConfigurationController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.OldConfigurationController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class GUIView extends Application {

    private LocalModel localModel;
    private  CommandParser commandParser;
    private  ClientController clientController;
    //private  SceneStatesConst scenePath;
    @Override
    public void start(Stage stage) throws Exception {
        List<Configuration> configs;
        Configuration.ConnectionType connectionType;
        String reconnectChoice;
        ClientInterface client;
        Parent root;
        this.commandParser = new CommandParser(new ClientController());
        this.clientController = commandParser.clientController();
        try {
            configs = ConfigurationManager.retrieveConfiguration();
            File url = new File(SceneStatesEnum.OldConfigurationScene.value());
            FXMLLoader loader = new FXMLLoader(url.toURL());
            root = loader.load();
            OldConfigurationController controller = loader.getController();
            controller.setCommandParser(this.commandParser);
            controller.setClientController(this.clientController);
            controller.setStage(stage);
            controller.setConfig(configs);
            controller.setUpConfigTable();
        } catch (RuntimeException e) {
            File url = new File(SceneStatesEnum.NewConfigurationScene.value());
            FXMLLoader loader = new FXMLLoader(url.toURL());
            root = loader.load();
            NewConfigurationController controller = loader.getController();
            controller.setCommandParser(this.commandParser);
            controller.setClientController(this.clientController);
            controller.setStage(stage);
        }
        Scene scene = new Scene(root);

        //scene.getStylesheets().add(getClass().getResource("/css/SetupScene.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
