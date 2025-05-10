package com.example.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "keycloak-admin")
public class KeycloakAdminClientProperties {

	private String realm;

	private String clientId;

	private String secret;

	private String url;
}
