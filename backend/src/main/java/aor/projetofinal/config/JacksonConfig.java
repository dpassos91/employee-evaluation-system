

package aor.projetofinal.config;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configures Jackson's ObjectMapper to serialize Java 8 date/time as ISO-8601 strings.
 * Applies globally to all REST endpoints.
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonConfig() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Force ISO-8601 string output
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
