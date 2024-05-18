package it.polimi.ingsw.gc19.View.GUI.Utils;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class CardView{

    private final String cardCode;
    private final ImageView front;
    private final ImageView back;

    public CardView(String code){
        this.cardCode = code;
        this.front = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + code + "_front.jpg")).toExternalForm()));
        this.back = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + code + "_back.jpg")).toExternalForm()));
    }

    public String getCardCode() {
        return cardCode;
    }

    public ImageView getFront() {
        return front;
    }

    public ImageView getBack() {
        return back;
    }

    public ImageView getOtherSide(ImageView imageView){
        if(this.front.equals(imageView)){
            return this.back;
        }
        else{
            return this.front;
        }
    }

    public boolean isUpSide(ImageView imageView){
        return this.front.equals(imageView);
    }

}