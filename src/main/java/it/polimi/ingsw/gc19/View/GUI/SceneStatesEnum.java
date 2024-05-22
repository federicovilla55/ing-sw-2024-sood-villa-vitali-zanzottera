package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.View.Listeners.ListenerType;

import java.util.List;

public enum SceneStatesEnum {
    StartScene("src/main/resources/fxml/Start.fxml"),
    LoginScene("src/main/resources/fxml/Login.fxml"),
    GameSelectionScene("src/main/resources/fxml/GameSelection.fxml"),
    PlayingAreaScene("src/main/resources/fxml/PlayingArea.fxml"),
    NewConfigurationScene( "src/main/resources/fxml/NewConfiguration.fxml"),
    OldConfigurationScene("src/main/resources/fxml/OldConfiguration.fxml"),
    SETUP_SCENE("src/main/resources/fxml/SetupScene.fxml"),
    RECONNECTION_SCENE("src/main/resources/fxml/ReconnectionWaitScene.fxml");

    private final String value;

    SceneStatesEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}