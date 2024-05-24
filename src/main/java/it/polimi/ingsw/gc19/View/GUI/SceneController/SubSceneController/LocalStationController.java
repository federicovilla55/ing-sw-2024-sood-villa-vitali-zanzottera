package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

//questo controllore si occupa solo di stampare il colore della pedina e la stazione di gioco
public class LocalStationController extends AbstractController {

    @FXML
    protected VBox leftVBox;
    @FXML
    protected VBox rightVBox;
    @FXML
    protected BorderPane borderPane;

    protected String nickOwner;

    public LocalStationController(AbstractController controller,String nickOwner) {
        super(controller);

        this.nickOwner = nickOwner;
    }

    @FXML
    protected void initialize(){
        this.leftVBox.getChildren().clear();
        this.rightVBox.getChildren().clear();

        initializePawns();
        initializeGameArea();
        initializeCards();
    }

    protected void initializePawns(){
        if(!this.nickOwner.equals(this.getLocalModel().getNickname())){
            this.rightVBox.getChildren().clear();
        }

        if(super.getLocalModel().getStations().get(nickOwner).getChosenColor() != null){
            rightVBox.getChildren().add(pawnFactory(super.getLocalModel().getStations().get(nickOwner).getChosenColor().toString()));
        }

        if(nickOwner.equals(this.getLocalModel().getFirstPlayer())){
            rightVBox.getChildren().add(pawnFactory("black"));
        }
    }

    protected void initializeCards(){
        if(this.nickOwner.equals(this.getLocalModel().getNickname())){
            if(super.getLocalModel().getPersonalStation().getPrivateGoalCardInStation() != null) {
                this.rightVBox.getChildren().add(new CardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation()));
            }

            this.leftVBox.getChildren().clear();

            for(PlayableCard p : this.getLocalModel().getPersonalStation().getCardsInHand()){
                CardButton button = new CardButton(p);

                button.setOnMouseClicked(button.getDefaultMouseClickedHandler());

                this.leftVBox.getChildren().add(button);
            }
        }
        else{
            this.leftVBox.getChildren().clear();

            for(var v : ((OtherStation) this.getLocalModel().getStations().get(this.nickOwner)).getBackCardHand()){
                this.leftVBox.getChildren().add(factoryUnswappableCard(v.x(), v.y()));
            }
        }
    }

    private ImageView factoryUnswappableCard(Symbol symbol, PlayableCardType type){
        ImageView imageView = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/images/back/" + type.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg"))
                       .toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);

        return imageView;
    }

    protected void initializeGameArea(){
        if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
            FXMLLoader loader;
            PlayingAreaController controller;
            try{
                loader = new FXMLLoader();
                loader.setLocation(new File("src/main/resources/fxml/PlayingArea.fxml").toURL());
                controller = new PlayingAreaController(this);
                loader.setController(controller);

                this.borderPane.setCenter(loader.load());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            controller.setCardGrid(super.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence());
        }
    }

    private ImageView pawnFactory(String name){
        ImageView color = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + name.toLowerCase() + "_pawn.png")).toExternalForm()));
        color.setPreserveRatio(true);
        color.setFitHeight(50);

        return color;
    }

}
