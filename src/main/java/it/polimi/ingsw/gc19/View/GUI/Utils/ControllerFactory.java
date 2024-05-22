package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.ChatController;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;

public class ControllerFactory {

    public static ChatController buildChatController(AbstractController abstractController){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/ChatScene.fxml").toURL());
            ChatController controller = new ChatController(abstractController);
            loader.setController(controller);

            //chat = loader.load();

            //rightVBox.getChildren().add(chat);

            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
