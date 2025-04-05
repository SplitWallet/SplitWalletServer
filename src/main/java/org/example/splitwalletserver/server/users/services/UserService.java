package org.example.splitwalletserver.server.users.services;


import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

public interface UserService {
    UserDTO createUser(UserDTO userRegistrationRecord);
    AccessTokenResponse login(LoginUserDTO loginUserDTO);
    UserRepresentation getUserById(String userId);
    void deleteUserById(String userId);
}
