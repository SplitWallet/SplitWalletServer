package org.example.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.codec.ErrorDecoder;
import org.example.authservice.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status()  >= 400 && response.status() < 500) {
                // Парсим JSON-ответ
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ErrorResponse errorResponse = mapper.readValue(response.body().asInputStream(), ErrorResponse.class);
                    return new CustomFeignException(errorResponse.getMessage(), response.status());
                } catch (IOException e) {
                    return new FeignException.NotFound("Not Found", response.request(), null, null);
                }
            }
            return FeignException.errorStatus(methodKey, response);
        };
    }
}

