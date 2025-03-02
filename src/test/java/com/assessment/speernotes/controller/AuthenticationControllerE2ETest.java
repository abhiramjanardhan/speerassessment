package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Optional<User> user = userRepository.findByEmail("e2etestuser@example.com");
        user.ifPresent(value -> userRepository.delete(value));

        Optional<User> wrongUser = userRepository.findByEmail("wronge2etestuser@example.com");
        wrongUser.ifPresent(value -> userRepository.delete(value));
    }

    @Test
    void testSignupSuccess() throws Exception {
        UserAuthDto newUser = new UserAuthDto("e2etestuser", "e2etestuser@example.com", "password123");
        String userJson = objectMapper.writeValueAsString(newUser);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    void testSignupDuplicateEmail() throws Exception {
        userRepository.save(new User("e2etestuser", "e2etestuser@example.com", "password123"));

        UserAuthDto newUser = new UserAuthDto("anotheruser", "e2etestuser@example.com", "password456");
        String userJson = objectMapper.writeValueAsString(newUser);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User already exists!"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Assuming user creation process works
        userRepository.save(new User("e2etestuser", "e2etestuser@example.com", "password123"));

        UserAuthDto loginUser = new UserAuthDto("e2etestuser", "e2etestuser@example.com", "password123");
        String loginJson = objectMapper.writeValueAsString(loginUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        UserAuthDto invalidUser = new UserAuthDto("wronge2euser", "wronge2etestuser@example.com", "wrongpassword");
        String loginJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid user for the email wronge2etestuser@example.com. Please try again!"));;
    }
}
