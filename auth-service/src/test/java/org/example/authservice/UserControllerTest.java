package org.example.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.authservice.controller.UserController;
import org.example.authservice.db.User;
import org.example.authservice.dto.GoogleToken;
import org.example.authservice.dto.LoginUserDTO;
import org.example.authservice.dto.UserDTO;
import org.example.authservice.service.UserServiceImpl;
import org.keycloak.representations.AccessTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getCurrentUser_shouldReturnUser() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");
        when(userService.getCurrentUser()).thenReturn(mockUser);

        mockMvc.perform(get("/currentUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void addUser_shouldReturnJwtToken() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testUser");
        userDTO.setPassword("testPassword");

        String jwtToken = "mockJwtToken";
        AccessTokenResponse accessTokenResponse = mock(AccessTokenResponse.class);
        when(accessTokenResponse.getToken()).thenReturn(jwtToken);

        when(userService.createUser(any(UserDTO.class))).thenReturn(null);
        when(userService.login(any(LoginUserDTO.class))).thenReturn(accessTokenResponse);

        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwtToken").value(jwtToken));
    }

    @Test
    void loginUser_shouldReturnJwtToken() throws Exception {
        LoginUserDTO loginUserDTO = new LoginUserDTO("testUser", "testPassword");
        String jwtToken = "mockJwtToken";
        AccessTokenResponse accessTokenResponse = mock(AccessTokenResponse.class);
        when(accessTokenResponse.getToken()).thenReturn(jwtToken);

        when(userService.login(any(LoginUserDTO.class))).thenReturn(accessTokenResponse);

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwtToken").value(jwtToken));
    }

    @Test
    void loginUserByGoogle_shouldReturnJwtToken() throws Exception {
        GoogleToken googleToken = new GoogleToken("mockGoogleToken");
        String googleAccessToken = "mockGoogleAccessToken";

        GoogleToken returnedGoogleToken = new GoogleToken(googleAccessToken);

        when(userService.loginByGoogle(any(GoogleToken.class))).thenReturn(returnedGoogleToken);

        mockMvc.perform(post("/login/google")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(googleToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwtToken").value(googleAccessToken));
    }

}
