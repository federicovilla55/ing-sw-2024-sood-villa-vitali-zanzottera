package it.polimi.ingsw.gc19.Enums;

import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;

/**
 * This enum represents the state of the players:
 * active (connected with regular heartbeats) or inactive (probably
 * not connected with no heartbeats for more than {@link ServerSettings#MAX_DELTA_TIME_BETWEEN_HEARTBEATS}).
 */
public enum State {
    ACTIVE, INACTIVE
}
