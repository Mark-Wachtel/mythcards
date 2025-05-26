package server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Import;

@Import(JwtConverterConfig.class)
@Configuration
public class SecurityConfig {

    /**
     * Diese Chain gilt nur für alles, was auf /ws startet:
     *   - HTTP-Upgrade (SockJS-Info/WebSocket) wird ungeschützt durchgelassen.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain webSocketChain(HttpSecurity http) throws Exception {
        http
          .securityMatcher("/ws/**")
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .csrf(csrf -> csrf.disable())
          .cors(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Die „normale“ API‐Chain für alle /api/** Endpoints und /auth/**.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http,
                                        JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/auth/**").permitAll()
              .requestMatchers("/error").permitAll()
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .requestMatchers("/api/**").authenticated()
              .anyRequest().denyAll()
          )
          .oauth2ResourceServer(oauth2 ->
              oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
          );
        return http.build();
    }
}