package it.polimi.ingsw.gc19.View.Listeners;

public interface Observable {

    void attachObserver(GenericObserver genericObserver);

    void removeObserver(GenericObserver genericObserver);

}
