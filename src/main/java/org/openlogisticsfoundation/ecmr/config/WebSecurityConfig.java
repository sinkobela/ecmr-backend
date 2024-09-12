/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.domain.models.ApiKeyAuthentication;
import org.openlogisticsfoundation.ecmr.domain.services.ApiKeyAuthenticationService;
import org.openlogisticsfoundation.ecmr.domain.services.RoleService;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Service
@AllArgsConstructor
public class WebSecurityConfig {

    private UserRepository userRepository;
    private final ApiKeyAuthenticationService apiKeyAuthenticationService;
    private final RoleService roleService;

    public interface Jwt2AuthoritiesConverter extends Converter<Jwt, Collection<? extends GrantedAuthority>> {
    }

    public interface Jwt2AuthenticationConverter extends Converter<Jwt, AbstractAuthenticationToken> {
    }

    @Bean
    public Jwt2AuthoritiesConverter authoritiesConverter() {
        return jwt -> this.roleService.mapRolesToGrantedAuthorities(getRoles(jwt));
    }

    private Set<String> getRoles(Jwt jwt) {
        Optional<String> emailOpt = Optional.ofNullable(jwt.getClaimAsString("email")).or(() -> Optional.ofNullable(jwt.getClaimAsString("upn")));
        if (emailOpt.isEmpty()) {
            return Set.of();
        }
        Optional<UserEntity> userOpt = userRepository.findByEmailAndDeactivatedFalse(emailOpt.get());
        return userOpt.map(userEntity -> this.roleService.mapUserRoleToStrings(userEntity.getRole())).orElseGet(Set::of);
    }

    @Bean
    public Jwt2AuthenticationConverter authenticationConverter(Jwt2AuthoritiesConverter authoritiesConverter) {
        return jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt));
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Jwt2AuthenticationConverter authenticationConverter) throws Exception {
        http.oauth2ResourceServer(oauthConfig -> oauthConfig.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(authenticationConverter)));
        http.addFilterBefore(new ApiKeyFilter(this.apiKeyAuthenticationService), UsernamePasswordAuthenticationFilter.class);
        http.cors(x -> {
        });
        http.csrf(AbstractHttpConfigurer::disable);
        http.anonymous(AbstractHttpConfigurer::disable);
        http.exceptionHandling(x -> x.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @AllArgsConstructor
    static class ApiKeyFilter extends GenericFilterBean {
        private final ApiKeyAuthenticationService apiKeyAuthenticationService;

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
                ServletException {
            String apiKeyString = ((HttpServletRequest) servletRequest).getHeader("X-API-KEY");
            if (StringUtils.isNoneBlank(apiKeyString)) {
                UUID apiKey = UUID.fromString(apiKeyString);
                Optional<ApiKeyAuthentication> apiKeyAuthentication = this.apiKeyAuthenticationService.getApiKeyAuthentication(apiKey);
                SecurityContextHolder.getContext().setAuthentication(apiKeyAuthentication.orElse(null));
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

}
