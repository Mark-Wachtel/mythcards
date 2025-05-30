package server;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtConverterConfig {

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
	    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

	    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
	        List<String> roles = jwt.getClaimAsStringList("roles");
	        if (roles == null) return List.of();

	        return roles.stream()
	                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
	                .collect(Collectors.toList());
	    });

	    return converter;
	}
}