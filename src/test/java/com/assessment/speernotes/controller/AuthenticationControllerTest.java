package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.service.UsersService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersService usersService;

    @AfterEach
    public void cleanUp() {
        // Clean up after each test to ensure no test data persists
        usersService.deleteUserByEmail("test@example.com");
        usersService.deleteUserByEmail("newuser@example.com");
        usersService.deleteUserByEmail("wrong@example.com");
        usersService.deleteUserByEmail("valid@example.com");
    }

    @Test
    void testSignup_UserAlreadyExists() {
        // Given
        UserAuthDto userAuthDto = new UserAuthDto("testuser", "test@example.com", "password123");
        usersService.createUser(userAuthDto); // create user in the test DB

        // When
        HttpEntity<UserAuthDto> requestEntity = new HttpEntity<>(userAuthDto);
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/signup", HttpMethod.POST, requestEntity, String.class);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User already exists!", response.getBody());
    }

    @Test
    void testSignup_UserCreatedSuccessfully() {
        // Given
        UserAuthDto userAuthDto = new UserAuthDto("newuser", "newuser@example.com", "password123");

        // When
        HttpEntity<UserAuthDto> requestEntity = new HttpEntity<>(userAuthDto);
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/signup", HttpMethod.POST, requestEntity, String.class);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully!", response.getBody());
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Given
        UserAuthDto userAuthDto = new UserAuthDto("wronguser", "wrong@example.com", "wrongpassword");

        // When
        HttpEntity<UserAuthDto> requestEntity = new HttpEntity<>(userAuthDto);
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/login", HttpMethod.POST, requestEntity, String.class);

        // Then
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Invalid user for the email wrong@example.com. Please try again!"));
    }

    @Test
    void testLogin_ValidCredentials() {
        // Given
        UserAuthDto userAuthDto = new UserAuthDto("validuser", "valid@example.com", "password123");
        usersService.createUser(userAuthDto); // Create user for testing login

        // When
        HttpEntity<UserAuthDto> requestEntity = new HttpEntity<>(userAuthDto);
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/login", HttpMethod.POST, requestEntity, String.class);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}