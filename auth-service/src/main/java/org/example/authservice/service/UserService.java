package org.example.authservice.service;



import org.example.authservice.db.User;
import org.example.authservice.dto.LoginUserDTO;
import org.example.authservice.dto.UserDTO;
import org.keycloak.representations.AccessTokenResponse;

public interface UserService {
    UserDTO createUser(UserDTO userRegistrationRecord);
    AccessTokenResponse login(LoginUserDTO loginUserDTO);
    User getUserById(String userId);
    void deleteUserById(String userId);
}
