package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.NetworkManagementInterface;

import java.util.*;

/**
 * This empty interface represents a generic client that is
 * configurable (extends {@link ConfigurableClient}), can interact using the network
 * (extends {@link NetworkManagementInterface}) and can be used to play a game
 * (extends {@link GameManagementInterface}).
 */
public interface ClientInterface extends ConfigurableClient, NetworkManagementInterface, GameManagementInterface {

}