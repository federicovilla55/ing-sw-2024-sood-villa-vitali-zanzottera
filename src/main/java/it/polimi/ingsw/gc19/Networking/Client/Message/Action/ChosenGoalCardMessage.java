package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

public class ChosenGoalCardMessage implements ActionMessage{

    private final String chosenCard;

    public ChosenGoalCardMessage(String chosenCard){
        this.chosenCard = chosenCard;
    }

    public String getChosenCard() {
        return this.chosenCard;
    }

}
