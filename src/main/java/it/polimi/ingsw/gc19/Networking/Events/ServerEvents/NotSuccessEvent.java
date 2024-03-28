package it.polimi.ingsw.gc19.Networking.Events.ServerEvents;

public class NotSuccessEvent extends Event {
    String Error;
    public NotSuccessEvent(String Error)
    {
        this.Error = Error;
    }
}
