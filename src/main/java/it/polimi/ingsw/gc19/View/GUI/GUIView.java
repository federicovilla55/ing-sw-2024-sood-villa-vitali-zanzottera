package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.NewConfigurationController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.OldConfigurationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        List<Configuration> configs;
        Parent root;

        CommandParser commandParser = new CommandParser(new ClientController());
        ClientController clientController = commandParser.getClientController();

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));

        try {
            configs = ConfigurationManager.retrieveConfiguration();

            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory((c) -> new OldConfigurationController(clientController, commandParser, stage));

            loader.setLocation(new File(SceneStatesEnum.OLD_CONFIGURATION_SCENE.value()).toURL());

            root = loader.load();

            ((OldConfigurationController) loader.getController()).setConfig(new ArrayList<>(configs));
            ((OldConfigurationController) loader.getController()).setUpConfigTable();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        }
        catch (RuntimeException e) {
            FXMLLoader loader = new FXMLLoader();
            //loader.setControllerFactory((c) -> new NewConfigurationController(clientController, commandParser, stage));

            loader.setLocation(new File(SceneStatesEnum.NEW_CONFIGURATION_SCENE.value()).toURL());
            loader.setController(new NewConfigurationController(clientController, commandParser, stage));

            root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
