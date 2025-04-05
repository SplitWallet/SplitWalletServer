package org.example.splitwalletserver.server.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakAdminClientProperties.class)
public class KeycloakAdminClientConfig {

	/**
	 * Админский клиент для keycloak
	 */
	@Bean
	public Keycloak keycloak(KeycloakAdminClientProperties properties) {
		return KeycloakBuilder.builder()
						.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
						.serverUrl(properties.getUrl())
						.realm(properties.getRealm())
						.clientId(properties.getClientId())
						.clientSecret(properties.getSecret())
						.build();
	}
}
