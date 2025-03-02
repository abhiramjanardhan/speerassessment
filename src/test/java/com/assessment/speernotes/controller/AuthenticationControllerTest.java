package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerUnitTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    public void testSignup_UserAlreadyExists() throws Exception {
        // Arrange
        UserAuthDto user = new UserAuthDto("test", "test@example.com", "password");
        when(usersService.isUserPresent(user.getEmail())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("{\"username\":\"test\", \"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User already exists!"));

        verify(usersService, times(1)).isUserPresent(user.getEmail());
        verify(usersService, never()).createUser(any());
    }

    @Test
    public void testSignup_SuccessfulRegistration() throws Exception {
        // Arrange
        UserAuthDto user = new UserAuthDto("test", "test@example.com", "password");
        when(usersService.isUserPresent(user.getEmail())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("{\"username\":\"test\", \"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        verify(usersService, times(1)).isUserPresent(user.getEmail());
        verify(usersService, times(1)).createUser(user);
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        UserAuthDto user = new UserAuthDto("test", "test@example.com", "wrongPassword");
        when(usersService.getUserJWT(user)).thenReturn("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"test\", \"email\":\"test@example.com\", \"password\":\"wrongPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid credentials"));

        verify(usersService, times(1)).getUserJWT(user);
    }

    @Test
    public void testLogin_SuccessfulLogin() throws Exception {
        // Arrange
        UserAuthDto user = new UserAuthDto("test", "test@example.com", "password");
        String jwtToken = "some-jwt-token";
        when(usersService.getUserJWT(user)).thenReturn(jwtToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(jwtToken));

        verify(usersService, times(1)).getUserJWT(user);
    }
}