package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.View.GUI.GUISettings;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import static it.polimi.ingsw.gc19.View.GUI.GUISettings.*;

/**
 * This class represents a generic button with a {@link GoalCard} as its graphic.
 */
public class GoalCardButton extends Button{

    /**
     * Attached {@link GoalCard}
     */
    private final GoalCard card;

    /**
     * Front side of the corresponding {@link #card}
     */
    private final ImageView front;

    /**
     * Back side of the corresponding {@link #card}
     */
    private final ImageView back;

    /**
     * <code>true</code> if card has been rendered in its up state (e.g. front side is
     * currently shown)
     */
    private boolean isUp;

    public GoalCardButton(GoalCard card){
        super();

        this.card = card;
        this.front = new ImageView(CardImageLoader.getImage(card, CardOrientation.UP));
        this.back = new ImageView(CardImageLoader.getImage(card, CardOrientation.DOWN));

        this.isUp = true;

        this.front.setPreserveRatio(true);
        clipCardImage(this.front);
        this.back.setPreserveRatio(true);
        clipCardImage(this.back);

        super.setPadding(Insets.EMPTY);
        super.setBorder(Border.EMPTY);

        super.setGraphic(this.front);
        super.setStyle("""
                        -fx-focus-color: transparent;
                        -fx-background-color: transparent;
                        """);

        buildTooltip();
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

        buildTooltip();
    }

    /**
     * Build a {@link Tooltip} describing the {@link GoalCard} associated to the button.
     * Duration of the tooltip is indefinite, and it follows mouse movement inside button.
     */
    private void buildTooltip(){
        Tooltip infos = new Tooltip("Goal card ard description");

        infos.setText(this.card.getCardDescription());

        infos.setShowDelay(Duration.seconds(1));
        infos.setShowDuration(Duration.INDEFINITE);
        infos.setHideDelay(Duration.INDEFINITE);

        this.setOnMouseEntered(event -> {
            Tooltip.install(this, infos);
        });

        this.setOnMouseExited(event ->  {
            infos.hide();
            Tooltip.uninstall(this, infos);
        });

        this.setOnMouseMoved(event -> {
            infos.setX(event.getScreenX() + 5);
            infos.setY(event.getScreenY() + 5);
        });
    }

    /**
     * Clips the {@param cardImage} by a {@link Rectangle} with rounded corners.
     * @param cardImage the {@link ImageView} to be clipped.
     */
    private void clipCardImage(ImageView cardImage){
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(cardImage.fitWidthProperty());
        rectangle.heightProperty().bind(cardImage.fitWidthProperty().multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH));

        rectangle.arcWidthProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));
        rectangle.arcHeightProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

        cardImage.setClip(rectangle);
    }

    /**
     * Getter for {@link GoalCard} associated to the button
     * @return the {@link GoalCard} associated to the button
     */
    public Card getCard(){
        return this.card;
    }

    /**
     * Getter for {@link ImageView} of the side currently seen
     * @return the {@link ImageView} of the side currently seen.
     */
    public ImageView getSide(){
        if(this.isUp){
            return this.front;
        }
        else{
            return this.back;
        }
    }

    /**
     * Getter for default {@link MouseEvent} handler: when user double-click on the card, this swaps.
     * @return the default {@link MouseEvent} handler for the card.
     */
    public EventHandler<MouseEvent> getDefaultMouseClickedHandler(){
        return (event) -> {
            if(event.getClickCount() == 2) {
                this.swap();
            }
        };
    }

    /**
     * Swaps side of the card.
     */
    public void swap(){
        this.isUp = !this.isUp;
        super.setGraphic(getSide());
    }

}