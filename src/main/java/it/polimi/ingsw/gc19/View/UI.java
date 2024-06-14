package it.polimi.ingsw.gc19.View;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;

/**
 * All user interfaces (UI) must implement this interface,
 * so that other local components can interact with it
 */
public interface UI{

    /**
     * Setter for {@link LocalModel}
     * @param localModel the {@link LocalModel} to be set
     */
    void setLocalModel(LocalModel localModel);

    /**
     * To notify a generic error to the UI
     * @param errorDescription a brief string description
     *                         of the error
     */
    void notifyGenericError(String errorDescription);

    /**
     * To notify a generic message to the view
     * @param message the string version of the message to be notified
     */
    void notify(String message);

}