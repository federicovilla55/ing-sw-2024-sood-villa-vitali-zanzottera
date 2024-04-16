package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.ArrayList;

public class LocalModel {
    private Station personalStation;
    private ArrayList<Station> otherStations;
    private LocalTable table;

    LocalModel(){
        personalStation = null;
        otherStations = null;
    }

    public void setPersonalStation(Station localStation) {
        this.personalStation = localStation;
    }

    public Station getPersonalStation() {
        return this.personalStation;
    }

    public void setOtherStations(ArrayList<Station> otherStations) {
        this.otherStations = otherStations;
    }

    public ArrayList<Station> getOtherStations() {
        return this.otherStations;
    }

    public void setTable(LocalTable table) {
        this.table = table;
    }

    public LocalTable getTable() {
        return table;
    }
}
