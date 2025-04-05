package org.example.splitwalletserver.server.users.services;


import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserDTO userRegistrationRecord);
    Optional<String> login(LoginUserDTO loginUserDTO);
    UserRepresentation getUserById(String userId);
    void deleteUserById(String userId);
}
