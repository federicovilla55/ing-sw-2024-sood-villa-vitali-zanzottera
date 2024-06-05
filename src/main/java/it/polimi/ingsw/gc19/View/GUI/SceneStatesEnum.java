package it.polimi.ingsw.gc19.View.GUI;

public enum SceneStatesEnum {
    START_SCENE("fxml/Start.fxml"),
    LOGIN_SCENE("fxml/Login.fxml"),
    GAME_SELECTION_SCENE("fxml/GameSelection.fxml"),
    PLAYING_AREA_SCENE("fxml/PlayingArea.fxml"),
    NEW_CONFIGURATION_SCENE("fxml/NewConfiguration.fxml"),
    OLD_CONFIGURATION_SCENE("fxml/OldConfiguration.fxml"),
    SETUP_SCENE("fxml/SetupScene.fxml"),
    RECONNECTION_SCENE("fxml/ReconnectionWaitScene.fxml");

    private final String value;

    SceneStatesEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}