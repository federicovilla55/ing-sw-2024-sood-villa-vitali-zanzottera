package it.polimi.ingsw.gc19.View.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class GUIView extends Application {
    private Parent createContent() {
        return new StackPane(new Text("Hello World"));
    }

    @Override
    public void start(Stage stage) throws Exception {
        //stage.setScene(new Scene(createContent(), 300, 300));
        //
        File url = new File("src/main/resources/fxml/PROVA.fxml");
        Parent root = FXMLLoader.load(url.toURL());
        stage.setScene(new Scene(root, 300, 275));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
