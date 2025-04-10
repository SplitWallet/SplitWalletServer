package org.example.splitwalletserver.server.users.services;


import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.example.splitwalletserver.server.users.model.User;
import org.keycloak.representations.AccessTokenResponse;

public interface UserService {
    UserDTO createUser(UserDTO userRegistrationRecord);
    AccessTokenResponse login(LoginUserDTO loginUserDTO);
    User getUserById(String userId);
    void deleteUserById(String userId);
}
