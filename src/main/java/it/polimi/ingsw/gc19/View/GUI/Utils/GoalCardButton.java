package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;

public class GoalCardButton extends Button{

    private final GoalCard card;
    private final ImageView front;
    private final ImageView back;
    private boolean isUp;

    public GoalCardButton(GoalCard card){
        super();

        this.card = card;
        this.front = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.getCardCode() + "_front.jpg")).toExternalForm()));
        this.back = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.getCardCode() + "_back.jpg")).toExternalForm()));

        this.isUp = true;

        this.front.setPreserveRatio(true);
        this.front.setFitWidth(200);
        clipCardImage(this.front);
        this.back.setPreserveRatio(true);
        this.back.setFitWidth(200);
        clipCardImage(this.back);

        super.setPadding(Insets.EMPTY);
        super.setBorder(Border.EMPTY);

        /*super.setOnMouseEntered(event -> {
            Node source = (Node) event.getSource();

            Tooltip infos = new Tooltip();
            infos.setText(this.card.getCardDescription());

            infos.show(source, event.getSceneX() + 50, event.getScreenY());
        });*/

        super.setGraphic(this.front);
        super.setStyle("""
                        -fx-focus-color: transparent;
                        -fx-background-color: transparent;
                        """);
    }

    public GoalCardButton(GoalCard card, Region region, double scale) {
        this(card);

        this.front.fitHeightProperty().bind(region.heightProperty().multiply(scale));
        this.back.fitHeightProperty().bind(region.heightProperty().multiply(scale));
    }

    public GoalCardButton(GoalCard card, Stage stage, Double scaleX, Double scaleY) {
        this(card);

        if(scaleX != null) {
            this.front.fitWidthProperty().bind(stage.widthProperty().multiply(scaleX));
            this.back.fitWidthProperty().bind(stage.widthProperty().multiply(scaleX));
        }

        if(scaleY != null){
            this.front.fitHeightProperty().bind(stage.heightProperty().multiply(scaleY));
            this.back.fitHeightProperty().bind(stage.heightProperty().multiply(scaleY));
        }
    }

    public GoalCardButton(GoalCard card, Region region, Double scaleX, Double scaleY) {
        this(card);

        if(scaleX != null) {
            this.front.fitWidthProperty().bind(region.widthProperty().multiply(scaleX));
            this.back.fitWidthProperty().bind(region.widthProperty().multiply(scaleX));
        }

        if(scaleY != null){
            this.front.fitHeightProperty().bind(region.heightProperty().multiply(scaleY));
            this.back.fitHeightProperty().bind(region.heightProperty().multiply(scaleY));
        }
    }

    private void clipCardImage(ImageView cardImage){
        double CARD_PIXEL_HEIGHT = 558.0;
        double CARD_PIXEL_WIDTH = 832.0;

        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(cardImage.fitWidthProperty());
        rectangle.heightProperty().bind(cardImage.fitWidthProperty().multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH));

        double CORNER_RADIUS = 27.0;
        rectangle.arcWidthProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));
        rectangle.arcHeightProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

        cardImage.setClip(rectangle);
    }

    public Card getCard(){
        return this.card;
    }

    public void showSide(CardOrientation orientation){
        if(orientation == CardOrientation.UP){
            this.isUp = true;
            super.setGraphic(this.front);
        }
        else{
            this.isUp = false;
            super.setGraphic(this.back);
        }
    }

    public ImageView getOtherSide(){
        if(this.isUp){
            return this.back;
        }
        else{
            return this.front;
        }
    }

    public ImageView getSide(){
        if(this.isUp){
            return this.front;
        }
        else{
            return this.back;
        }
    }

    public CardOrientation getCardOrientation(){
        return this.isUp ? CardOrientation.UP : CardOrientation.DOWN;
    }

    public EventHandler<MouseEvent> getDefaultMouseClickedHandler(){
        return (event) -> {
            if(event.getClickCount() == 2) {
                this.swap();
            }
        };
    }

    public void swap(){
        this.isUp = !this.isUp;
        super.setGraphic(getSide());
    }

}