package com.example.emsreportingservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simpler development. Reconsider for production.
                .authorizeHttpRequests(auth -> auth
                        // Explicitly permit WebSocket and SockJS endpoints
                        .requestMatchers(
                                new AntPathRequestMatcher("/ws/**"), // For the WebSocket handshake endpoint
                                new AntPathRequestMatcher("/topic/**"), // For STOMP messages over WebSocket
                                new AntPathRequestMatcher("/app/**")   // For STOMP messages from client (if you add @MessageMapping)
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll() // This is still very broad. Consider securing other endpoints.
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}