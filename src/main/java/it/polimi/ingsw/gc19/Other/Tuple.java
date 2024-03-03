package it.polimi.ingsw.gc19.Other;

public class Tuple<T, S>{

    private T firstValue;
    private S secondValue;

    public Tuple(T firstValue, S secondValue){

        this.firstValue = firstValue;

        this.secondValue = secondValue;

    }

    public T getFirstValue(){

        return firstValue;

    }

    public S getSecondValue(){

        return secondValue;

    }

    public void setFirstValue(T firstValue){

        this.firstValue = firstValue;

    }

    public void setSecondValue(S secondValue){

        this.secondValue = secondValue;

    }

}
