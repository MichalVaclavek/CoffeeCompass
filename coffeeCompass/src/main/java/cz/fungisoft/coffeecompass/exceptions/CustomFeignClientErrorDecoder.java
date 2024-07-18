package cz.fungisoft.coffeecompass.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CustomFeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignExceptionMessage message = new FeignExceptionMessage();
        if (response.body() != null) {
            try (InputStream bodyIs = response.body().asInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                message = mapper.readValue(bodyIs, FeignExceptionMessage.class);
            } catch (IOException e) {
                return new Exception(e.getMessage());
            }
        }
        return switch (response.status()) {
            case 400 :
                log.warn("Bad request error for Feign client.");
                yield new BadRequestException(message.getMessage() != null ? message.getMessage() : "Bad Request");
            case 404 :
                log.warn("Not found error for Feign client.");
                yield new EntityNotFoundException(message.getMessage() != null ? message.getMessage() : "Not found");
            default : yield  errorDecoder.decode(methodKey, response);
        };
    }
}
