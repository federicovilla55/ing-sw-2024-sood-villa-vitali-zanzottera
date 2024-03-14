package it.polimi.ingsw.gc19.Model.Enums;

import com.fasterxml.jackson.annotation.*;
import it.polimi.ingsw.gc19.Model.Card.Corner;

import java.util.EnumSet;
import java.util.Optional;

@JsonTypeName("symbol")
public enum Symbol implements Corner{

    ANIMAL, VEGETABLE, INSECT, MUSHROOM, INK,FEATHER, SCROLL;
    //ANIMAL("ANIMAL"),
    //VEGETABLE("VEGETABLE"),
    //@JsonProperty("INSECT")
    //INSECT("INSECT"),
    //@JsonProperty("MUSHROOM")
    //MUSHROOM("MUSHROOM"),
    //@JsonProperty("INK")
    //INK("INK"),
    //@JsonProperty("FEATHER")
    //FEATHER("FEATHER"),
    //@JsonProperty("SCROLL")
   //SCROLL("SCROLL");

    //private final String value;

    /*@JsonCreator
    Symbol(@JsonProperty("value") String value){
        this.value = value;
    };*/

    public static EnumSet<Symbol> getResources(){
        return EnumSet.of(ANIMAL, VEGETABLE, INSECT, MUSHROOM);
    }

    public static EnumSet<Symbol> getObjects(){
        return EnumSet.of(INK, SCROLL, FEATHER);
    }

    @Override
    public boolean hasSymbol() {
        return true;
    }

    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.of(this);
    }

}
