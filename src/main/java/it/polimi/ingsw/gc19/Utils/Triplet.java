package it.polimi.ingsw.gc19.Utils;

import java.io.Serializable;

public record Triplet<R, T, S> (
        R x,
        T y,
        S z
){}