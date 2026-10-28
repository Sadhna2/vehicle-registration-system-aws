package com.nexturn.vehicleregistration.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Documents stage 3 payment, RTO workflow, RC issuance and administrative endpoints.
 * Uses the API Gateway stage prefix for Swagger's Try it out requests.
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Defines API information and the request base path.
     *
     * @param stagePath API Gateway stage, such as /stage3-payment-and-workflow, or empty locally
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI vehicleRegistrationApi(
            @Value("${API_STAGE_PATH:}") String stagePath) {

        String basePath = stagePath.isBlank() ? "/" : stagePath;

        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Registration System AWS")
                        .version("stage3-payment-and-workflow")
                        .description("Stage 3 - Simulated payment, verification, inspection, RC issuance, employee/account management, reports and audit"))
                .servers(List.of(new Server().url(basePath)));
    }
}