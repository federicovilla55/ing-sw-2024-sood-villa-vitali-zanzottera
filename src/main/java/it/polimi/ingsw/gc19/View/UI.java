package it.polimi.ingsw.gc19.View;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;

public interface UI{

    void setLocalModel(LocalModel localModel);

    void notifyGenericError(String errorDescription);

    void notify(String message);

}
