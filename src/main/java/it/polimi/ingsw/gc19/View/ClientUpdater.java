package it.polimi.ingsw.gc19.View;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GameLocalView.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientUpdater implements PropertyChangeListener {
    private LocalModel localModel;

    public ClientUpdater(){
        localModel = null;
    }

    public void setLocalModel(LocalModel localModel) {
        this.localModel = localModel;
        localModel.addAllPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyChanged = event.getPropertyName();

        switch (propertyChanged) {
            case "chat":
                ArrayList<Message> newMessages = (ArrayList<Message>) event.getNewValue();
                System.out.println("Aggiornamento chat");
                break;
            case "personalStation":
                PersonalStation newPersonalStation = (PersonalStation) event.getNewValue();
                System.out.println("Aggiornamento stazione personale");
                break;
            case "otherStations":
                ConcurrentHashMap<String, OtherStation> otherStations = (ConcurrentHashMap<String, OtherStation>) event.getNewValue();
                System.out.println("Aggiornamento altre stazioni");
                break;
            case "table":
                LocalTable table = (LocalTable) event.getNewValue();
                System.out.println("Aggiornamento tavolo");
                break;
        }

    }
}
