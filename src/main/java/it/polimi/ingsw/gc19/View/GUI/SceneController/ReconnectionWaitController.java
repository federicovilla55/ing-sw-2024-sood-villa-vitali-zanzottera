package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReconnectionWaitController extends AbstractController implements StateListener {

    @FXML
    private Pane pane;

    @FXML
    private StackPane stackPane;

    private final Stage prevStage;

    private final ArrayList<Circle> circles;

    private Thread thread;

    public ReconnectionWaitController(AbstractController controller, Stage prevStage) {
        super(controller);

        this.circles = new ArrayList<>();
        this.prevStage = prevStage;
    }

    @FXML
    public void initialize(){
        buildBackScene();

        double rho = 50;

        for(int theta = 0; theta < 12; theta++){
            Circle circle = new Circle(5);

            circle.centerXProperty().bind(this.pane.widthProperty().divide(2).add(rho * Math.cos(Math.toRadians((double) 360 / 12 * theta))));
            circle.centerYProperty().bind(this.pane.heightProperty().divide(2).add(rho * Math.sin(Math.toRadians((double) 360 / 12 * theta))));

            circle.setFill(Color.GREY);

            circles.add(circle);

            pane.getChildren().add(circle);
        }

        pane.requestLayout();

        initThread();
    }

    private void initThread(){
        thread = new Thread(() -> {
            Circle currrentCircle = circles.getFirst();

            while(!Thread.currentThread().isInterrupted()){
                currrentCircle.setFill(Color.BLACK);

                try{
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                }

                currrentCircle.setFill(Color.GREY);

                if(circles.getLast().equals(currrentCircle)){
                    currrentCircle = circles.getFirst();
                }
                else{
                    currrentCircle = circles.get(circles.indexOf(currrentCircle) + 1);
                }
            }
        });

        thread.start();
    }

    private void buildBackScene(){
        System.out.println(this.prevStage.getScene().getRoot().getChildrenUnmodifiable().size());
        if(!this.prevStage.getScene().getRoot().getChildrenUnmodifiable().isEmpty()) {
            for (Node n : this.prevStage.getScene().getRoot().getChildrenUnmodifiable()) {
                this.stackPane.getChildren().add(n);
                n.setOpacity(0.75);
            }

            this.stackPane.requestLayout();
        }
    }

    @Override
    public void notify(ViewState viewState) {
        this.thread.interrupt();

        //@TODO: what to do in PAUSE? And END?

        switch (viewState){
            case ViewState.SETUP -> changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            case ViewState.PICK, ViewState.OTHER_TURN, ViewState.PLACE -> changeToNextScene(SceneStatesEnum.PlayingAreaScene);
            case ViewState.NOT_GAME -> changeToNextScene(SceneStatesEnum.GameSelectionScene);
            case ViewState.NOT_PLAYER -> changeToNextScene(SceneStatesEnum.NewConfigurationScene);
        }
    }

}
