package org.example.expensesservice.security;

import org.example.expensesservice.config.TokenConverterProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtTokenConverter implements Converter<Jwt, JwtAuthenticationToken> {

	private static final String RESOURCE_ACCESS = "resource_access";
	private static final String ROLES = "roles";
	private static final String ROLE_PREFIX = "ROLE_";
	private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
	private final TokenConverterProperties properties;

	public KeycloakJwtTokenConverter(
					JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
					TokenConverterProperties properties) {
		this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
		this.properties = properties;
	}

	@Override
	public JwtAuthenticationToken convert(@NonNull Jwt jwt) {
		Stream<SimpleGrantedAuthority> accesses = Optional.of(jwt)
						.map(token -> token.getClaimAsMap(RESOURCE_ACCESS))
						.map(claimMap -> (Map<String, Object>) claimMap.get(properties.getResourceId()))
						.map(resourceData -> (Collection<String>) resourceData.get(ROLES))
						.stream()
						.map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
						.distinct();

		Set<GrantedAuthority> authorities = Stream
						.concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), accesses)
						.collect(Collectors.toSet());

		String principalClaimName = properties.getPrincipalAttribute()
						.map(jwt::getClaimAsString)
						.orElse(jwt.getClaimAsString(JwtClaimNames.SUB));

		return new JwtAuthenticationToken(jwt, authorities, principalClaimName);
	}
}
