package it.polimi.ingsw.gc19.Model.Enums;

import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Model.Card.Corner;

import java.util.EnumSet;
import java.util.Optional;

@JsonTypeName("symbol")
public enum Symbol implements Corner{
    ANIMAL, VEGETABLE, INSECT, MUSHROOM, INK, FEATHER, SCROLL;

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

    public Optional<Symbol> getSymbol(){
        return Optional.of(this);
    }

}
