package it.polimi.ingsw.gc19.View.GUI;

public enum SceneStatesEnum {
    START_SCENE("it/polimi/ingsw/gc19/fxml/Start.fxml"),
    LOGIN_SCENE("it/polimi/ingsw/gc19/fxml/Login.fxml"),
    GAME_SELECTION_SCENE("it/polimi/ingsw/gc19/fxml/GameSelection.fxml"),
    PLAYING_AREA_SCENE("it/polimi/ingsw/gc19/fxml/PlayingArea.fxml"),
    NEW_CONFIGURATION_SCENE("it/polimi/ingsw/gc19/fxml/NewConfiguration.fxml"),
    OLD_CONFIGURATION_SCENE("it/polimi/ingsw/gc19/fxml/OldConfiguration.fxml"),
    SETUP_SCENE("it/polimi/ingsw/gc19/fxml/SetupScene.fxml"),
    RECONNECTION_SCENE("it/polimi/ingsw/gc19/fxml/ReconnectionWaitScene.fxml");

    private final String value;

    SceneStatesEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}