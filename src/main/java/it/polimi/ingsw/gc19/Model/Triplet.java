package it.polimi.ingsw.gc19.Model;

import java.io.Serializable;

public record Triplet<R, T, S> (
        R x,
        T y,
        S z
){}