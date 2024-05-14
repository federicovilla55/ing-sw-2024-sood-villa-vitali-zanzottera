package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used for managing configurations. It is responsible
 * for saving, retrieving and deleting configurations from disk.
 * Configurations are store in JSON files
 */
public class ConfigurationManager {

    /**
     * This method save a {@link Configuration} on disk in a JSON format.
     * The name of the file is the player name.
     * @param configuration the {@link Configuration} to save on disk
     * @throws RuntimeException if file cannot be created or an {@link IOException} occurs
     * while performing the action
     */
    public static void saveConfiguration(Configuration configuration) throws RuntimeException{
        File configFile, configFolder;

        try {
            configFolder = new File(ClientSettings.CONFIG_FILE_PATH);
            if(configFolder.exists()) configFolder.delete();
            configFolder.mkdir();

            configFile = new File(ClientSettings.CONFIG_FILE_PATH + configuration.getNick() + ".json");
            if(configFile.createNewFile()){
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(configFile, configuration);
            }
            else{
                throw new RuntimeException("Could not create file!");
            }
        }
        catch (IOException ioException){
            throw new RuntimeException("Could not create file!");
        };

    }

    /**
     * This method retrieves a configuration from disk, loading it from file
     * with name equals to {@param nick}.
     * @param nick the nickname of the player owning the configuration file
     * @return the {@link Configuration} read from file
     * @throws IllegalStateException if the configuration does not exist on the path
     * @throws IOException if an error occurs while performing the requested action
     */
    public static Configuration retriveConfiguration(String nick) throws IllegalStateException, IOException {
        File configFile;
        Configuration configuration;

        try{
            configFile = new File(ClientSettings.CONFIG_FILE_PATH + nick + ".json");
            if(configFile.exists() && !configFile.isDirectory()){
                ObjectMapper objectMapper = new ObjectMapper();
                configuration = objectMapper.readValue(configFile, Configuration.class);
            }
            else{
                throw new IllegalStateException("Cannot retrieve configuration because configuration file specified does not exist!");
            }
        }
        catch (IOException e) {
            throw new IOException(e);
        }

        return configuration;
    }

    /**
     * This method is used to retrieve a configuration from disk. It works if and only
     * if in the specified path ({@link ClientSettings#CONFIG_FILE_PATH}) there is only one file,
     * otherwise an {@link IllegalStateException} is raised.
     * @return the {@link Configuration} read from file
     * @throws IllegalStateException if JSON file is corrupted or there is more then one file
     * in the path {@link ClientSettings#CONFIG_FILE_PATH}
     */
    public static Configuration retriveConfiguration() throws IllegalStateException{
        File configFile;
        Configuration configuration;

        try{
            configFile = new File(ClientSettings.CONFIG_FILE_PATH);
            if(configFile.exists() && configFile.isDirectory()) {
                if (Arrays.stream(Objects.requireNonNull(configFile.listFiles()))
                        .filter(File::isFile)
                        .filter(f -> f.getName().endsWith(".json"))
                        .count() != 1) {
                    throw new IllegalStateException();
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    configuration = objectMapper.readValue(Objects.requireNonNull(configFile.listFiles())[0], Configuration.class);
                }
            }
            else throw new IllegalStateException();
        }
        catch (StreamReadException | DatabindException e) {
            throw new IllegalStateException(e);
        }
        catch (IOException e) {
            throw new IllegalStateException();
        }

        return configuration;
    }

    /**
     * This method deletes a configuration file in path {@link ClientSettings#CONFIG_FILE_PATH}.
     * It works if and only if there is only one file in the path.
     * @throws IllegalStateException if there is more than one file in the specified path.
     */
    public static void deleteConfiguration() throws IllegalStateException{
        File configFile;

        configFile = new File(ClientSettings.CONFIG_FILE_PATH);
        if(Arrays.stream(Objects.requireNonNull(configFile.listFiles()))
                 .filter(File::isFile)
                 .filter(f -> f.getName().endsWith(".json"))
                 .count() != 1){
            throw new IllegalStateException();
        }
        else{
            if(configFile.delete()){
                System.err.println("[CONFIG]: config file correctly deleted.");
            }
        }
    }

    /**
     * This method deletes the configuration file with name equals to {@param nick}.
     * @param nick the nickname of the player for which it is necessary to delete its configuration file
     */
    public static void deleteConfiguration(String nick){
        File configFile;

        configFile = new File(ClientSettings.CONFIG_FILE_PATH + nick + ".json");
        if(configFile.delete()){
            System.err.println("[CONFIG]: config file correctly deleted.");
        }
    }

}