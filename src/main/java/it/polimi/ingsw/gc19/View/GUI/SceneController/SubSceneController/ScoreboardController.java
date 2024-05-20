package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoreboardController extends AbstractController implements StationListener {
    private static final double[][] scoreboardPositions = {
            {313, 2199}, // 0
            {591, 2199}, // 1
            {869, 2199},  // 2
            {1007, 1945.5},  // 3
            {729.5, 1945.5},  // 4
            {452, 1945.5},  // 5
            {173.5, 1945.5},  // 6
            {173.5, 1691},  // 7
            {452, 1691},  // 8
            {729.5, 1691},  // 9
            {1007, 1691},   // 10
            {1007, 1436.5},  // 11
            {729.5, 1436.5},    // 12
            {452, 1436.5},  // 13
            {173.5, 1436.5},   // 14
            {173.5, 1182},  // 15
            {452, 1182},  // 16
            {729.5, 1182},  // 17
            {1007, 1182},  // 18
            {1007, 927.5},  // 19
            {590, 802.5},   // 20
            {174.5, 927,5},   // 21
            {174.5, 673},   // 22
            {174.5, 418.5},   // 23
            {335, 210},   // 24
            {591, 164},   // 25
            {847, 210},   // 26
            {1008, 419},   // 27
            {1008, 673}, // 28
            {591, 472}   // 29
    };

    private final HashMap<String, ImageView> pawnScoreboard = new HashMap<>();

    private Image scoreboardImage;

    private ImageView scoreboardView;

    private Pane scoreboardPane;

    private double pawnSize;

    private double ratio;

    protected ScoreboardController(AbstractController controller) {
        super(controller);

        controller.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    public void initialize(){
        scoreboardImage = new Image(getClass().getResourceAsStream("/score_table.jpg"));
        if (scoreboardImage.isError()) {
            System.err.println("Error while loading scoreboard");
            return;
        }

        double scoreboardHeight = 800;
        double proportions = scoreboardImage.getWidth() / scoreboardImage.getHeight();
        ratio = scoreboardHeight / scoreboardImage.getHeight();
        pawnSize = scoreboardImage.getHeight() * ratio * proportions / 8;
        scoreboardView = new ImageView(scoreboardImage);
        scoreboardView.setPreserveRatio(true);
        scoreboardView.setFitWidth(scoreboardHeight * proportions);
        scoreboardView.setFitHeight(scoreboardHeight);

        scoreboardPane = new Pane();
        scoreboardPane.getChildren().add(scoreboardView);

        ArrayList<LocalStationPlayer> stations = new ArrayList<>(getLocalModel().getStations().values());

        for(LocalStationPlayer station : stations){
            placePawn(station);
        }
    }

    private void placePawn(LocalStationPlayer station) {
        if(station.getChosenColor() == null) return;
        String pawnColor = station.getChosenColor().toString();
        int scoredPoints = station.getNumPoints();
        Image pawnImage = new Image(getClass().getResourceAsStream("/pawns/" + pawnColor + "_pawn.png"));
        if (pawnImage.isError()) {
            System.err.println("Error while loading image for " + pawnColor + " pawn.");
            return;
        }

        if (pawnScoreboard.containsKey(pawnColor)) {
            scoreboardPane.getChildren().remove(pawnScoreboard.get(pawnColor));
        }

        double[] position = scoreboardPositions[scoredPoints];
        ImageView pawnImageView = new ImageView(pawnImage);
        pawnImageView.setFitWidth(pawnSize);
        pawnImageView.setFitHeight(pawnSize);
        pawnImageView.setPreserveRatio(true);
        pawnImageView.setLayoutX(scoreboardView.getTranslateX() + position[0] * ratio - pawnSize / 2);
        pawnImageView.setLayoutY(scoreboardView.getTranslateY() + position[1] * ratio - pawnSize / 2);

        scoreboardPane.getChildren().add(pawnImageView);

        pawnScoreboard.put(pawnColor, pawnImageView);
    }

    @Override
    public void notify(PersonalStation localStationPlayer) {
        Platform.runLater(() -> {
            placePawn(localStationPlayer);
        });
    }

    @Override
    public void notify(OtherStation otherStation) {
        Platform.runLater(() -> {
            placePawn(otherStation);
        });
    }

    @Override
    public void notifyErrorStation(String... varArgs) {
        // Errors should be handled in another part of the GUI view...
    }
}
