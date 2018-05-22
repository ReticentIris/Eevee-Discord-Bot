package io.reticent.eevee.configuration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.exc.InvalidConfigurationKeyException;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Log4j2
public class Configuration {
    @NonNull
    private String filePath;
    private Map<String, Object> data;

    public Object readObject(@NonNull String key) throws InvalidConfigurationException {
        if (data == null) {
            loadData();
        }

        if (data.containsKey(key)) {
            log.debug(String.format("Found value for key: %s. Attempting to return.", key));
            return data.get(key);
        } else {
            log.error(String.format("Attempted to read String for invalid key: %s.", key));
            throw new InvalidConfigurationKeyException(
                String.format("The requested key \"%s\" does not exist.", key)
            );
        }
    }

    public String readString(@NonNull String key) throws InvalidConfigurationException {
        return (String) readObject(key);
    }

    public int readInt(@NonNull String key) throws InvalidConfigurationException {
        return (Integer) readObject(key);
    }

    public double readDouble(@NonNull String key) throws InvalidConfigurationException {
        return (Double) readObject(key);
    }

    public boolean readBoolean(@NonNull String key) throws InvalidConfigurationException {
        return (Boolean) readObject(key);
    }

    public List<String> readStringList(@NonNull String key) throws InvalidConfigurationException {
        return (List<String>) readObject(key);
    }

    public List<Integer> readIntArray(@NonNull String key) throws InvalidConfigurationException {
        return (List<Integer>) readObject(key);
    }

    public List<Double> readDoubleArray(@NonNull String key) throws InvalidConfigurationException {
        return (List<Double>) readObject(key);
    }

    public List<Boolean> readBooleanArray(@NonNull String key) throws InvalidConfigurationException {
        return (List<Boolean>) readObject(key);
    }

    private void loadData() throws InvalidConfigurationException {
        File file = new File(filePath);

        if (!file.exists()) {
            log.error(String.format("The configuration with path \"%s\" could not be found.", filePath));
            throw new InvalidConfigurationException(
                String.format("The configuration with path \"%s\" could not be found.", filePath)
            );
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> genericTypeReference = new TypeReference<HashMap<String, Object>>() {
            };
            data = objectMapper.<HashMap<String, Object>>readValue(file, genericTypeReference);
        } catch (JsonParseException e) {
            log.error(
                String.format("Error mapping configuration values. The configuration with path \"%s\" could not be parsed as valid JSON.", filePath)
            );
            throw new InvalidConfigurationException(
                String.format("The configuration with path \"%s\" could not be parsed as valid JSON.", filePath)
            );
        } catch (JsonMappingException e) {
            log.error(
                String.format("Error mapping configuration values. The configuration with path \"%s\" could not be parsed as valid JSON.", filePath)
            );
            throw new InvalidConfigurationException(
                String.format("Error mapping configuration values. The configuration with path \"%s\" could not be parsed as valid JSON.", filePath)
            );
        } catch (IOException e) {
            log.error(
                String.format("Unexpected IO exception occurred. The configuration with path \"%s\" could not be loaded.", filePath)
            );
            throw new InvalidConfigurationException(
                String.format("Unexpected IO exception occurred. The configuration with path \"%s\" could not be loaded.", filePath)
            );
        }
    }
}
