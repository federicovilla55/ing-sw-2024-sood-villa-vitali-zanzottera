package it.polimi.ingsw.gc19.Tuple;

import it.polimi.ingsw.gc19.Enums.Symbol;

public class Tuple<T, S> {
    private final T x;
    private final S y;

    public Tuple(T x, S y){
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public S getY() {
        return y;
    }

}
