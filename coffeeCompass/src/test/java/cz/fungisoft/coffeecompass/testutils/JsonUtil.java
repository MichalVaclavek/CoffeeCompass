package cz.fungisoft.coffeecompass.testutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * Used mainly for integration testing to create JSON Requestbody
 * from object to REST requests sent from test client to server.
 * 
 * @author Michal V.
 *
 */
public class JsonUtil {

    public static byte[] toJson(Object object) throws IOException {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }
}
