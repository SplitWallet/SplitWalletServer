package org.example.splitwalletserver.server.security;

import org.example.splitwalletserver.server.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.util.Collections;

@Component
public class AuthProviderImpl implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthProviderImpl(UserDetailsServiceImpl userDetailsServiceImpl, PasswordEncoder passwordEncoder) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();

        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(name);

        String password = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(password, userDetailsImpl.getPassword())){
            throw new BadCredentialsException("Incorrect password");
        }

        return new UsernamePasswordAuthenticationToken(userDetailsImpl, password, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
