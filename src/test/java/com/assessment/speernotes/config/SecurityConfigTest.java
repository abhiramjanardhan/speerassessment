package com.assessment.speernotes.config;

import com.assessment.speernotes.requests.JwtAuthentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtAuthentication jwtAuthentication;

    @BeforeEach
    public void setUp() {
        // Ensure the Spring context loads and beans are available
        assertNotNull(securityFilterChain, "SecurityFilterChain should be available in the context");
        assertNotNull(authenticationManager, "AuthenticationManager should be available in the context");
        assertNotNull(passwordEncoder, "PasswordEncoder should be available in the context");
        assertNotNull(jwtAuthentication, "JwtAuthentication filter should be available in the context");
    }

    @Test
    public void testSecurityFilterChain() {
        // Check if the security filter chain bean is available in the application context
        assertNotNull(securityFilterChain, "SecurityFilterChain bean should be present in the context");
    }

    @Test
    public void testAuthenticationManager() {
        // Ensure the AuthenticationManager is correctly wired
        assertNotNull(authenticationManager, "AuthenticationManager bean should be present in the context");
    }

    @Test
    public void testPasswordEncoder() {
        // Test password encoder to verify it works
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "PasswordEncoder should correctly match the raw and encoded passwords.");
    }

    @Test
    public void testJwtAuthenticationFilter() {
        // Verify that JwtAuthentication filter is available in the context
        assertNotNull(jwtAuthentication, "JwtAuthentication filter should be available in the context");
    }
}