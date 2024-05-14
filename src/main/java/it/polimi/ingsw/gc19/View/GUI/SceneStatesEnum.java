package it.polimi.ingsw.gc19.View.GUI;

public enum SceneStatesEnum {
    StartScene("src/main/resources/fxml/Start.fxml"),
    LoginScene("src/main/resources/fxml/Login.fxml"),
    GameSelectionScene("src/main/resources/fxml/GameSelection.fxml"),
    PlayingAreaScene("src/main/resources/fxml/PlayingArea.fxml"),
    NewConfigurationScene( "src/main/resources/fxml/NewConfiguration.fxml"),
    OldConfigurationScene("src/main/resources/fxml/OldConfiguration.fxml");

    private final String value;

    SceneStatesEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}