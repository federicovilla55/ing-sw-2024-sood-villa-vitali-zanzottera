package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.View.GUI.SceneController.GUIController;
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
import javafx.util.Duration;

import java.util.*;


/**
 * The class represent a controller for the Scoreboard element in the GUI.
 * The class controls the display and updating of pawns on the scoreboard based on the player scores.
 */
public class ScoreboardController extends GUIController implements StationListener {
    /**
     * The array contains the positions in pixels from the top left corner of each point in the scoreboard.
     * Those positions are absolute (so they are calculated considering the full size of the scoreboard image) and
     * therefore they will be modified in each resize.
     */
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

    /**
     * The Image contains the image of the blue pawn.
     */
    private final Image bluePawnImage;
    /**
     * The Image contains the image of the red pawn.
     */
    private final Image redPawnImage;
    /**
     * The Image contains the image of the green pawn.
     */
    private final Image greenPawnImage;
    /**
     * The Image contains the image of the yellow pawn.
     */
    private final Image yellowPawnImage;

    /**
     * A hashmap that connects each color, represented as a lowercase string, to its imageview.
     * This is used when updating the position of the pawns on the scoreboard.
     */
    private final HashMap<String, ImageView> pawnScoreboard = new HashMap<>();
    /**
     * A hashmap that connects each point in the scoreboard (numbers between 0 and 29) to an array that contains
     * the ImageView of the pawn in that positions.
     */
    private final HashMap<Integer, ArrayList<ImageView>> pawnPositions = new HashMap<>();

    /**
     * The scoreboard image
     */
    private Image scoreboardImage;

    /**
     * An ImageView with the scoreboard image
     */
    private ImageView scoreboardView;

    public Pane scoreboardPane;

    /**
     * A double that represents the dimensions of the image representing the pawns.
     */
    private double pawnSize;

    /**
     * The proportions of the scoreboard.
     */
    private double proportions;

    /**
     * The dimensions of the scoreboard compared to its original dimensions.
     */
    private double ratio;

    public ScoreboardController(GUIController controller) {
        super(controller);

        bluePawnImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/blue_pawn.png")));
        redPawnImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/red_pawn.png")));
        greenPawnImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/green_pawn.png")));
        yellowPawnImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/yellow_pawn.png")));

        controller.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    /**
     * To initialize the ScoreboardControlled by creating the images, controlling the local model if there are
     * stations and pawn and placing them accordingly.
     * Listeners to update all pawns positions and size are added.
     */
    public void initialize(){
        scoreboardImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/score_table.jpg")));
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


    /**
     * Method used to update all pawns including their size and position of the board.
     */
    public void updateAllPawns(){
        ratio = scoreboardView.getFitHeight() / scoreboardImage.getHeight();
        pawnSize = scoreboardImage.getHeight() * ratio * proportions / 8;
        ArrayList<LocalStationPlayer> stations = new ArrayList<>(getLocalModel().getStations().values());
        for(LocalStationPlayer station : stations){
            placePawn(station);
        }
    }

    /**
     * Method used to place the pawn of a related station given the station from which take the color and the score.
     * @param station the station from which to take the points and the color of the pawn.
     */
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

        pawnImageView.setPreserveRatio(true);
        pawnImageView.setFitWidth(pawnSize);
        pawnImageView.setFitHeight(pawnSize);
        pawnImageView.setLayoutX(scoreboardView.getX() + basePosition[0] * ratio - pawnSize / 2);
        pawnImageView.setLayoutY(scoreboardView.getY() + basePosition[1] * ratio - pawnSize / 2);


        pawnsAtPosition.add(pawnImageView);

        updatePositions(scoredPoints);

        pawnPositions.put(scoredPoints, pawnsAtPosition);

        addMouseHoverListener(pawnImageView);
    }

    /**
     * If there is more than one pawn in a position on the scoreboard those should be moved so that more than
     * one pawn can be seen in that position.
     * An offset is used so that the pawns are always moved in the same way.
     * @param numPoints the point position in the scoreboard that needs an update.
     */
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

    /**
     * This method is used to notify the {@link ScoreboardController} about {@link PersonalStation} events.
     * The method updates the position of the pawn of that station using the passed value.
     * @param localStationPlayer is the {@link PersonalStation} that has changed
     */
    @Override
    public void notify(PersonalStation localStationPlayer) {
        Platform.runLater(() -> {
            placePawn(localStationPlayer);
        });
    }

    /**
     * This method is used to notify the {@link ScoreboardController} about {@link OtherStation} events.
     * The method updates the position of the pawn of that station using the passed value.
     * @param otherStation is the {@link OtherStation} that has changed
     */
    @Override
    public void notify(OtherStation otherStation) {
        Platform.runLater(() -> {
            placePawn(otherStation);
        });
    }

    /**
     * This method is not implemented as those kind of errors be handled in another part of the GUI view.
     * @param varArgs strings describing the error
     */
    @Override
    public void notifyErrorStation(String... varArgs) {
        // Errors should be handled in another part of the GUI view...
    }

    /**
     * The method implements a listener to the location of the mouse so that if a mouse is over a pawn, that pawn
     * is brought behind the scoreboard. This is useful if a user wants to see the points of a player but the pawn
     * is hiding the points number in the scoreboard.
     * @param pawnImageView the ImageView that we want to be hidden when a mouse is above that.
     */
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
