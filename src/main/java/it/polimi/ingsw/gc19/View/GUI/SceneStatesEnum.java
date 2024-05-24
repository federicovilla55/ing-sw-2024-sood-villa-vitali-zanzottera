package it.polimi.ingsw.gc19.View.GUI;

public enum SceneStatesEnum {
    START_SCENE("src/main/resources/fxml/Start.fxml"),
    LOGIN_SCENE("src/main/resources/fxml/Login.fxml"),
    GAME_SELECTION_SCENE("src/main/resources/fxml/GameSelection.fxml"),
    PLAYING_AREA_SCENE("src/main/resources/fxml/PlayingArea.fxml"),
    NEW_CONFIGURATION_SCENE("src/main/resources/fxml/NewConfiguration.fxml"),
    OLD_CONFIGURATION_SCENE("src/main/resources/fxml/OldConfiguration.fxml"),
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