package pairro.network.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Protocol {

  private static final ObjectMapper MAPPER = new ObjectMapper().configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    false
  );

  public record Envelope(String type, Map<String, String> fields) {
    public Envelope {
      if (type == null) {
        type = "";
      }

      if (fields == null) {
        fields = Map.of();
      }
    }
  }

  private static final Envelope EMPTY = new Envelope("", Map.of());

  public static String encode(String type) {
    return encode(type, Map.of());
  }

  public static String encode(String type, Map<String, String> fields) {
    try {
      return MAPPER.writeValueAsString(new Envelope(type, fields));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to encode message: " + type, e);
    }
  }

  public static Envelope decode(String message) {
    if (message == null) {
      return EMPTY;
    }

    try {
      return MAPPER.readValue(message, Envelope.class);
    } catch (JsonProcessingException e) {
      return EMPTY;
    }
  }
}
