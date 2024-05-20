package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.Card;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;

import java.util.Objects;

public class CardButton extends Button{

    private final Card card;
    private final ImageView front;
    private final ImageView back;
    private boolean isUp;

    public CardButton(Card card){
        super();

        this.card = card;
        this.front = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.getCardCode() + "_front.jpg")).toExternalForm()));
        this.back = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.getCardCode() + "_back.jpg")).toExternalForm()));

        this.isUp = true;

        this.front.setPreserveRatio(true);
        this.front.setFitWidth(300);
        this.back.setPreserveRatio(true);
        this.back.setFitWidth(300);

        super.setPadding(Insets.EMPTY);
        super.setBorder(Border.EMPTY);

        /*super.setOnMouseEntered(event -> {
            Node source = (Node) event.getSource();

            Tooltip infos = new Tooltip();
            infos.setText(this.card.getCardDescription());

            infos.show(source, event.getSceneX() + 50, event.getScreenY());
        });*/

        super.setGraphic(this.front);
    }

    public ImageView getFront() {
        return front;
    }

    public ImageView getBack() {
        return back;
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
            if(event.getClickCount() == 1) {
                this.swap();
            }
        };
    }

    public boolean isUpSide(){
        return this.isUp;
    }

    public void swap(){
        this.isUp = !this.isUp;
        super.setGraphic(getSide());
    }

}