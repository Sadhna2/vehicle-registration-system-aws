package com.nexturn.vehicleregistration.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Keeps JSON dates consistent with Swagger and the React frontend.
 * The Lambda archetype enables MVC explicitly, so configure its active
 * converters rather than relying on Spring Boot MVC defaults.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    /** Returns LocalDate and Instant fields as ISO date strings, without changing the database. */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                jsonConverter.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
        }
    }
}
