package it.polimi.ingsw.gc19.View.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class SetupSceneTest extends Application {

    @Test
    public void testChooseColor() throws IOException {
        Parent root;
        root = new FXMLLoader(new File(SceneStatesEnum.SETUP_SCENE.value()).toURL()).load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root;
        root = new FXMLLoader(new File(SceneStatesEnum.SETUP_SCENE.value()).toURL()).load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args){
        launch((String) null);
    }
}
