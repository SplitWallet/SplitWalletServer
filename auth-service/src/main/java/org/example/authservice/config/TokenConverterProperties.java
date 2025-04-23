package org.example.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Setter
@Getter
public class TokenConverterProperties {
	@Value("${keycloak.resource}")
	private String resourceId;

	@Value("${keycloak.principal-attribute}")
	private String principalAttribute;

	public Optional<String> getPrincipalAttribute() {
		return Optional.ofNullable(principalAttribute);
	}
}
