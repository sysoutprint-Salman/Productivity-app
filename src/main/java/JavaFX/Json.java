package JavaFX;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Json {
    /*
    * This class handles Jackson mapping and serializing/deserializing
    * of java objects to json, along with centralizing configuration for Jackson.
    * */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static String JsonBuilder(Map<String, Object> jsonMap) {
        try {
            return MAPPER.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build JSON", e);
        }
    }

    private Json() {}
}
