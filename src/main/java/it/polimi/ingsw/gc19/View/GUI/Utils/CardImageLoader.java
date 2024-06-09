package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to cache frequently used images for
 * GUI, in order to reduce significantly the amount of memory
 * required by the client application
 */
public class CardImageLoader {

    /**
     * This attribute contains all {@link Image} that
     * other classes have requested to {@link CardImageLoader} to load
     */
    private static final Map<String, Image> loadedImages = new ConcurrentHashMap<>();

    /**
     * This method loads and returns the {@link Image} corresponding
     * to the required {@link Card} and its {@link CardOrientation}
     * @param card the {@link Card} for which {@link Image} must be loaded
     * @param orientation the {@link CardOrientation} of the card
     * @return the {@link Image} describing that card with the specified orientation
     */
    public static Image getImage(Card card, CardOrientation orientation) {
        if (!loadedImages.containsKey(card.getCardCode().concat(orientation.toString()))) {
            loadedImages.put(card.getCardCode().concat(orientation.toString()), new Image(Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/" + card.getCardCode() + "_"
                    + (orientation.equals(CardOrientation.UP) ? "front" : "back") + ".jpg")).toExternalForm()));
        }
        return loadedImages.get(card.getCardCode().concat(orientation.toString()));
    }

    /**
     * This method loads and returns the {@link Image} corresponding
     * to the required {@link PlayableCardType} and its {@link Symbol}
     * @param symbol the {@link Symbol} of the {@link PlayableCardType} to load
     * @param cardType the {@link PlayableCardType} to load
     * @return the {@link Image} describing the required {@link PlayableCardType} and its {@link Symbol}
     */
    public static Image getBackImage(Symbol symbol, PlayableCardType cardType) {
        String name = symbol.toString().concat(cardType.toString());
        if (!loadedImages.containsKey(name)) {
            loadedImages.put(name, new Image(
                    Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/back/" + cardType.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg"))
                            .toExternalForm()));
        }
        return loadedImages.get(name);
    }

    /**
     * Getter for back of {@link GoalCard}
     * @return an {@link ImageView} describing the back of a {@link GoalCard}
     */
    public static ImageView getBackImageView() {
        if (!loadedImages.containsKey("goal")) {
            loadedImages.put("goal", new Image(Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/back/goal.jpg")).toExternalForm()));
        }
        return new ImageView(loadedImages.get("goal"));
    }
}