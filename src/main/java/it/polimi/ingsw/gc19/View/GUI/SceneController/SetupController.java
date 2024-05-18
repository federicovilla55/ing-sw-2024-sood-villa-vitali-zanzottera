package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardView;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupController extends AbstractController implements SetupListener {

    private CardView goalCard1, goalCard2;

    private CardView initialCard;

    @FXML
    private VBox vBox, rightVBox;

    @FXML
    private HBox hbox, goalCardsHBox, initialCardHBox;

    @FXML
    public void initialize(){
        buildAvailableColorsHBox(List.of(Color.BLUE, Color.RED, Color.GREEN));
        buildPrivateGoalCardSelectionHBox();
        buildInitialCardHBox();

        try{
            File url = new File("src/main/resources/fxml/Chat.fxml");
            VBox chat = new FXMLLoader(url.toURL()).load();
            rightVBox.getChildren().add(chat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildInitialCardHBox(){
        //@TODO: if local model has no info about initial card display both side, else what user has chosen
        initialCard = new CardView("initial_06");

        List<Button> buttons = cardFactory(initialCard);
        this.initialCardHBox.getChildren().addAll(buttons);

        for(Button b : buttons){
            b.setOnMouseClicked(event -> {
                if(event.getClickCount() == 1){
                    b.setGraphic(initialCard.getOtherSide((ImageView) b.getGraphic()));
                }
                else{
                    if(initialCard.isUpSide((ImageView) b.getGraphic())){
                        getClientController().placeInitialCard(CardOrientation.UP);
                    }
                    else{
                        getClientController().placeInitialCard(CardOrientation.DOWN);
                    }
                    b.setDisable(true);
                }
            });
        }
    }

    private void buildPrivateGoalCardSelectionHBox(){
        //@TODO: if local model has no info about chosen goal card display both, else what user has chosen
        goalCard1 = new CardView("goal_01");
        goalCard2 = new CardView("goal_02");

        List<Button> privateGoalCards = cardFactory(goalCard1, goalCard2);
        for(Button button : privateGoalCards){
            button.setOnMouseClicked(event -> {
                if(event.getClickCount() == 1){
                    if(button.getId().toLowerCase().equals(goalCard1.getCardCode())){
                        button.setGraphic(goalCard1.getOtherSide((ImageView) button.getGraphic()));
                    }
                    else{
                        button.setGraphic(goalCard2.getOtherSide((ImageView) button.getGraphic()));
                    }
                }
                else{
                    for(Button b : privateGoalCards){
                        b.setDisable(true);
                    }
                    getClientController().chooseGoal(Integer.parseInt(button.getId().split("_")[1]));
                }
            });
        }

        goalCardsHBox.getChildren().addAll(privateGoalCards);
        //goalCardsHBox.getChildren().addAll(privateGoalCardsFactory(this.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()));
    }

    private ArrayList<Button> cardFactory(CardView ... goalCards){
        ArrayList<Button> buttons = new ArrayList<>();

        for(CardView s : goalCards){
            s.getFront().setPreserveRatio(true);
            s.getFront().setFitWidth(300);
            s.getBack().setPreserveRatio(true);
            s.getBack().setFitWidth(300);

            Button button = new Button();
            button.setId(s.getCardCode());
            button.setGraphic(s.getFront());
            button.setPadding(Insets.EMPTY);
            button.setBorder(Border.EMPTY);

            buttons.add(button);
        }

        return buttons;
    }

    private void buildAvailableColorsHBox(List<Color> availableColors){
        hbox.getChildren().addAll(colorButtonFactory(List.of(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)));
    }

    private ArrayList<Button> colorButtonFactory(List<Color> availableColors){
        ArrayList<Button> buttons = new ArrayList<>();

        for(Color c : availableColors){
            Circle circlePawn = new Circle(25);
            circlePawn.setFill(new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + c + "_pawn.png")).toExternalForm())));

            Button button = buildColorButton(c, circlePawn);

            buttons.add(button);
        }

        return buttons;
    }

    @NotNull
    private Button buildColorButton(Color c, Circle circlePawn) {
        Button button = new Button();
        button.setShape(new Circle(25));

        button.setGraphic(circlePawn);
        button.setPadding(Insets.EMPTY);

        button.setId(c + "_button");
        button.setOnAction(event -> {
            for(Node n : SetupController.this.hbox.getChildren()){
                n.setDisable(true);
            }
            SetupController.super.getClientController().chooseColor(Color.valueOf(button.getId().toUpperCase().split("_")[0]));
        });
        return button;
    }

    @Override
    public void notify(SetupEvent type) {
        switch (type){
            case SetupEvent.AVAILABLE_COLOR -> buildAvailableColorsHBox(List.of(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW));
            case SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD -> buildPrivateGoalCardSelectionHBox();
        }
    }

    @Override
    public void notify(SetupEvent type, String errorDescription){
        super.notify(errorDescription);
        switch (type){
            case SetupEvent.COLOR_NOT_AVAILABLE -> {
                this.hbox.getChildren().clear();
                buildAvailableColorsHBox(this.getLocalModel().getAvailableColors());
            }
        }
    }

}
