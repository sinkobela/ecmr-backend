/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.config;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
    @Value("${swagger.auth-url:#{null}}")
    private String authUrl;
    @Value("${swagger.token-url:#{null}}")
    private String tokenUrl;

    public SwaggerConfig(MappingJackson2HttpMessageConverter converter) {
        var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }

    @Bean
    public OpenAPI swaggerSystemApi() {
        return new OpenAPI().components(
                        new Components().addSecuritySchemes("OAUth2", oAuthSecurityScheme()))
                .security(Collections.singletonList(new SecurityRequirement().addList("OAUth2")))
                .info(new Info()
                        .title("eCMR - API")
                        .description("eCMR - API")
                        .version("v0.1").contact(new Contact()
                                .name("Open Logistics Foundation\n")
                                .url("https://openlogisticsfoundation.org/")
                                .email("info@openlogisticsfoundation.org")));
    }

    private SecurityScheme oAuthSecurityScheme() {
        OAuthFlows oAuthFlows = createOAuthFlows();
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .flows(oAuthFlows);

    }

    private OAuthFlows createOAuthFlows() {
        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .tokenUrl(this.authUrl)
                .authorizationUrl(this.tokenUrl);
        return new OAuthFlows().authorizationCode(authorizationCodeFlow);
    }
}
