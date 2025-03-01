package org.example.splitwalletserver.server.services;

import org.example.splitwalletserver.server.repositories.UserRepository;
import org.example.splitwalletserver.server.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository repository;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
                .findByName(username).map(UserDetailsImpl::new)
                .orElseGet(
                        () -> repository.findByEmail(username).map(UserDetailsImpl::new)
                        .orElseThrow(() -> new UsernameNotFoundException(username + " not found"))
                );
    }
}
