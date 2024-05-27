package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

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

    private final Image bluePawnImage;
    private final Image redPawnImage;
    private final Image greenPawnImage;
    private final Image yellowPawnImage;

    private final HashMap<String, ImageView> pawnScoreboard = new HashMap<>();
    private final HashMap<Integer, ArrayList<ImageView>> pawnPositions = new HashMap<>();

    private Image scoreboardImage;

    private ImageView scoreboardView;

    public Pane scoreboardPane;

    private double pawnSize;

    private double proportions;

    private double ratio;

    public ScoreboardController(AbstractController controller) {
        super(controller);

        bluePawnImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/blue_pawn.png")));
        redPawnImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/red_pawn.png")));
        greenPawnImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/green_pawn.png")));
        yellowPawnImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/yellow_pawn.png")));

        controller.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    public void initialize(){
        scoreboardImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/score_table.jpg")));
        if (scoreboardImage.isError()) {
            System.err.println("Error while loading scoreboard");
            return;
        }

        proportions = scoreboardImage.getWidth() / scoreboardImage.getHeight();
        scoreboardView = new ImageView(scoreboardImage);

        scoreboardView.setPreserveRatio(true);
        double factor = 3;
        scoreboardView.fitWidthProperty().bind(super.getStage().widthProperty().multiply(1 / factor));
        scoreboardView.fitHeightProperty().bind(super.getStage().heightProperty().multiply(1 / (factor * proportions)));

        ratio = scoreboardView.getFitHeight() / scoreboardImage.getHeight();

        scoreboardPane = new Pane();
        scoreboardPane.getChildren().add(scoreboardView);

        scoreboardView.fitHeightProperty().addListener((obs, oldVal, newVal) -> updateAllPawns());
        scoreboardView.fitWidthProperty().addListener((obs, oldVal, newVal) -> updateAllPawns());

        updateAllPawns();
    }


    public void updateAllPawns(){
        ratio = scoreboardView.getFitHeight() / scoreboardImage.getHeight();
        pawnSize = scoreboardImage.getHeight() * ratio * proportions / 8;
        ArrayList<LocalStationPlayer> stations = new ArrayList<>(getLocalModel().getStations().values());
        for(LocalStationPlayer station : stations){
            placePawn(station);
        }
    }


    public void placePawn(LocalStationPlayer station) {
        if (station.getChosenColor() == null) return;

        Color pawnColor = station.getChosenColor();
        String pawnColorString = pawnColor.toString().toLowerCase();

        int scoredPoints = station.getNumPoints();
        if (scoredPoints > 29) scoredPoints = 29;

        Image pawnImage = switch (pawnColor) {
            case BLUE -> bluePawnImage;
            case RED -> redPawnImage;
            case GREEN -> greenPawnImage;
            case YELLOW -> yellowPawnImage;
        };

        if (pawnImage.isError()) {
            System.err.println("Error while loading image for " + pawnColorString + " pawn.");
            return;
        }

        ImageView pawnImageView;

        if (pawnScoreboard.containsKey(pawnColorString)) {
            pawnImageView = pawnScoreboard.get(pawnColorString);
            for(var entry : pawnPositions.entrySet()) {
                if(entry.getValue().contains(pawnImageView)){
                    entry.getValue().remove(pawnImageView);
                    updatePositions(entry.getKey());
                    break;
                }
            }
        }else{
            pawnImageView = new ImageView(pawnImage);
            scoreboardPane.getChildren().add(pawnImageView);
            pawnScoreboard.put(pawnColorString, pawnImageView);
        }

        double[] basePosition = scoreboardPositions[scoredPoints];

        ArrayList<ImageView> pawnsAtPosition = pawnPositions.getOrDefault(scoredPoints, new ArrayList<>());

        pawnImageView.setFitWidth(pawnSize);
        pawnImageView.setFitHeight(pawnSize);
        pawnImageView.setPreserveRatio(true);
        pawnImageView.setLayoutX(scoreboardView.getX() + basePosition[0] * ratio - pawnSize / 2);
        pawnImageView.setLayoutY(scoreboardView.getY() + basePosition[1] * ratio - pawnSize / 2);


        pawnsAtPosition.add(pawnImageView);

        updatePositions(scoredPoints);

        pawnPositions.put(scoredPoints, pawnsAtPosition);

        addMouseHoverListener(pawnImageView);
    }

    private void updatePositions(int numPoints){
        double xOffset = 0;
        double yOffset = 0;
        double offsetSize = 23 * pawnSize / 40;


        for(Map.Entry<String, ImageView> entry : pawnScoreboard.entrySet()){
            ImageView pawnImageView = entry.getValue();
            ArrayList<ImageView> pawnsAtPosition = pawnPositions.getOrDefault(numPoints, new ArrayList<>());
            if(pawnsAtPosition.contains(pawnImageView)) {
                if (pawnsAtPosition.size() > 1) {
                    yOffset = switch (entry.getKey()) {
                        case "red" -> {
                            xOffset = offsetSize;
                            yield -offsetSize;
                        }
                        case "yellow" -> {
                            xOffset = -offsetSize;
                            yield -offsetSize;
                        }
                        case "blue" -> {
                            xOffset = offsetSize;
                            yield offsetSize;
                        }
                        case "green" -> {
                            xOffset = -offsetSize;
                            yield offsetSize;
                        }
                        default -> yOffset;
                    };
                }
                pawnImageView.setFitWidth(pawnSize);
                pawnImageView.setFitHeight(pawnSize);
                pawnImageView.setPreserveRatio(true);
                pawnImageView.setLayoutX(scoreboardView.getTranslateX() + scoreboardPositions[numPoints][0] * ratio + xOffset - pawnSize / 2);
                pawnImageView.setLayoutY(scoreboardView.getTranslateY() + scoreboardPositions[numPoints][1] * ratio + yOffset - pawnSize / 2);
            }
        }


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

    private void addMouseHoverListener(ImageView pawnImageView) {
        pawnImageView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            pawnImageView.toBack();


            Timeline beat = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), e -> {
                        pawnImageView.toFront();
                    })
            );
            beat.play();
        });
    }
}
