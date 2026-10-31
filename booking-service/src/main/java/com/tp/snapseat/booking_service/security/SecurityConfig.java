package com.tp.snapseat.booking_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestSecurityErrorHandlers errorHandlers
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public health/monitoring endpoints
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Payment service -> Booking service
                        .requestMatchers(
                                HttpMethod.POST,
                                "/internal/bookings/*/payment-result"
                        ).hasRole("SERVICE")

                        // Other internal endpoints
                        .requestMatchers("/internal/**")
                        .authenticated()

                        // Customer/Admin booking APIs
                        .requestMatchers("/api/**")
                        .authenticated()

                        // Everything else denied
                        .anyRequest()
                        .denyAll()
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(errorHandlers)
                                .accessDeniedHandler(errorHandlers)
                )

                // JWT Bearer authentication
                .oauth2ResourceServer(oauth ->
                        oauth
                                .authenticationEntryPoint(errorHandlers)
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                                )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${tickethub.security.jwt-secret}") String jwtSecret) {

        SecretKey key = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
