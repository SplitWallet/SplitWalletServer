package org.example.authservice.service;



import org.example.authservice.db.User;
import org.example.authservice.dto.GoogleToken;
import org.example.authservice.dto.LoginUserDTO;
import org.example.authservice.dto.UserDTO;
import org.keycloak.representations.AccessTokenResponse;

import java.io.IOException;

public interface UserService {
    UserDTO createUser(UserDTO userRegistrationRecord);
    AccessTokenResponse login(LoginUserDTO loginUserDTO);
    GoogleToken loginByGoogle(GoogleToken googleToken) throws IOException, InterruptedException;
    User getUserById(String userId);
    void deleteUserById(String userId);
}
