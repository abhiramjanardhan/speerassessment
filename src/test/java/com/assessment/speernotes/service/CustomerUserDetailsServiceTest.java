package com.assessment.speernotes.service;

import com.assessment.speernotes.model.User;
import com.assessment.speernotes.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CustomerUserDetailsServiceTest {
    @Mock
    private UsersRepository userRepository;

    @InjectMocks
    private CustomerUserDetailsService customerUserDetailsService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");  // Ideally should be an encoded password
    }

    @Test
    public void testLoadUserByUsername_UserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customerUserDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("test@example.com", userDetails.getUsername(), "Username should match the expected value");
        assertEquals("password123", userDetails.getPassword(), "Password should match the expected value");
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("USER")),
                "User should have the USER role");
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customerUserDetailsService.loadUserByUsername("notfound@example.com"),
                "Should throw UsernameNotFoundException when user is not found");
    }
}
