package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
            if(!configFolder.exists()) configFolder.mkdir();

            configFile = new File(ClientSettings.CONFIG_FILE_PATH + configuration.getNick() + ".json");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(configFile, configuration);
        }
        catch (IOException ioException){
            throw new RuntimeException("Could not create file!");
        };

    }

    /**
     * This method retrieves a configuration from disk, loading it from file
     * with name equals to <code>nick</code>.
     * @param nick the nickname of the player owning the configuration file
     * @return the {@link Configuration} read from file
     * @throws IllegalStateException if the configuration does not exist on the path
     * @throws IOException if an error occurs while performing the requested action
     */
    public static Configuration retrieveConfiguration(String nick) throws IllegalStateException, IOException {
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
    public static List<Configuration> retrieveConfiguration() throws IllegalStateException{
        File configFilePath;
        List<Configuration> configurations;


            configFilePath = new File(ClientSettings.CONFIG_FILE_PATH);
            if(configFilePath.exists() && configFilePath.isDirectory()) {
                if (Arrays.stream(Objects.requireNonNull(configFilePath.listFiles()))
                        .filter(File::isFile).noneMatch(f -> f.getName().endsWith(".json"))) {
                    throw new IllegalStateException();
                } else {

                    ObjectMapper objectMapper = new ObjectMapper();
                    configurations = Arrays.stream(Objects.requireNonNull(configFilePath.listFiles()))
                            .filter(File::isFile)
                            .filter(f -> f.getName().endsWith(".json"))
                            .flatMap(
                            c -> {
                                try {
                                    return Stream.of(objectMapper.readValue(c, Configuration.class));
                                } catch (IOException ignored) {
                                    return Stream.empty();
                                }
                            }
                    ).toList();
                }
            }
            else throw new IllegalStateException();

        return configurations;
    }

    /**
     * This method deletes the configuration file with name equals to <code>nick</code>>.
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