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
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png"))));

        try {
            configs = ConfigurationManager.retrieveConfiguration();

            FXMLLoader loader = new FXMLLoader();

            loader.setController(new OldConfigurationController(clientController, commandParser, stage));
            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.OLD_CONFIGURATION_SCENE.value()));

            root = loader.load();

            ((OldConfigurationController) loader.getController()).setConfig(new ArrayList<>(configs));
            ((OldConfigurationController) loader.getController()).setUpConfigTable();
        }
        catch (RuntimeException e) {
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.NEW_CONFIGURATION_SCENE.value()));
            loader.setController(new NewConfigurationController(clientController, commandParser, stage));

            root = loader.load();
        }

        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/background_light.png")));

        BackgroundSize backgroundSize = new BackgroundSize(360, 360, false, false, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);

        if (root instanceof Region) {
            ((Region) root).setBackground(new Background(background));
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(false);
        stage.sizeToScene();
        stage.setResizable(false);
        scene.getWindow().centerOnScreen();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
