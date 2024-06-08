package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A scene controller used to wait for reconnection
 * and signaling to user that network problems have arisen.
 */
public class ReconnectionWaitController extends GUIController implements StateListener {

    @FXML
    private Pane pane;

    @FXML
    private StackPane stackPane;

    private final ArrayList<Circle> circles;

    private Thread thread;

    public ReconnectionWaitController(GUIController controller) {
        super(controller);

        this.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);

        this.circles = new ArrayList<>();
    }

    /**
     * Initializes reconnection wait scene and defines spinning wheel.
     */
    @FXML
    private void initialize(){
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

    /**
     * Build thread responsible for making wheel spinning.
     */
    private void initThread(){
        thread = new Thread(() -> {
            Circle currrentCircle = circles.getFirst();

            while(!Thread.currentThread().isInterrupted()){
                currrentCircle.setFill(Color.BLACK);

                try{
                    TimeUnit.MILLISECONDS.sleep(300);
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

    /**
     * Used to notify {@link ReconnectionWaitController} about events
     * happened on {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        this.thread.interrupt();

        switch (viewState){
            case ViewState.SETUP -> changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            case ViewState.PICK, ViewState.OTHER_TURN, ViewState.PLACE, ViewState.PAUSE, ViewState.END -> changeToNextScene(SceneStatesEnum.PLAYING_AREA_SCENE);
            case ViewState.NOT_GAME -> changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            case ViewState.NOT_PLAYER -> changeToNextScene(SceneStatesEnum.NEW_CONFIGURATION_SCENE);
            default -> {
                return;
            }
        }

        super.getClientController().getListenersManager().removeListener(this);
    }

}