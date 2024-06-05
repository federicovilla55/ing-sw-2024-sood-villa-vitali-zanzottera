package it.polimi.ingsw.gc19.View.GUI.Utils;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CardImageLoader {

    private static final Map<String, ImageView> loadedImageViews = new ConcurrentHashMap<>();
    private static final Map<String, Image> loadedImages = new ConcurrentHashMap<>();

    public static Image getImage(Card card, CardOrientation orientation) {
        if (!loadedImages.containsKey(card.getCardCode().concat(orientation.toString()))) {
            loadedImages.put(card.getCardCode().concat(orientation.toString()), new Image(Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/" + card.getCardCode() + "_"
                    + (orientation.equals(CardOrientation.UP) ? "front" : "back") + ".jpg")).toExternalForm()));
        }
        return loadedImages.get(card.getCardCode().concat(orientation.toString()));
    }

    public static Image getBackImage(Symbol symbol, PlayableCardType cardType) {
        String name = symbol.toString().concat(cardType.toString());
        if (!loadedImages.containsKey(name)) {
            loadedImages.put(name, new Image(
                    Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/back/" + cardType.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg"))
                            .toExternalForm()));
        }
        return loadedImages.get(name);
    }

    public static ImageView getBackImageView() {
        if (!loadedImageViews.containsKey("goal")) {
            loadedImageViews.put("goal", new ImageView(new Image(Objects.requireNonNull(CardImageLoader.class.getClassLoader().getResource("it/polimi/ingsw/gc19/images/back/goal.jpg")).toExternalForm())));
        }
        return loadedImageViews.get("goal");
    }
}
