package com.assessment.speernotes.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.assessment.speernotes.exceptions.UserException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.assessment.speernotes.repository.*;
import com.assessment.speernotes.utils.*;
import com.assessment.speernotes.model.*;
import com.assessment.speernotes.model.dto.*;
import java.util.Optional;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {

    @Mock
    private UsersRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ConvertorUtil convertorUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    // Test for getAuthenticatedUser()
    @Test
    void testGetAuthenticatedUser_Success() {
        String email = "testuser@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");
        user.setPassword("password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(email, "password",
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User authenticatedUser = usersService.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertEquals(email, authenticatedUser.getEmail());
    }

    @Test
    void testGetAuthenticatedUser_Exception() {
        when(userRepository.findByEmail(anyString())).thenThrow(new UserException("User not found"));

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("nonexistent@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(UserException.class, () -> usersService.getAuthenticatedUser());
    }

    // Test for findUserByEmail()
    @Test
    void testFindUserByEmail_Success() {
        String email = "testuser@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = usersService.findUserByEmail(email);
        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
    }

    @Test
    void testFindUserByEmail_UserException() {
        String email = "testuser@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> usersService.findUserByEmail(email));
    }

    // Test for isUserPresent()
    @Test
    void testIsUserPresent_True() {
        String email = "testuser@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean isPresent = usersService.isUserPresent(email);
        assertTrue(isPresent);
    }

    @Test
    void testIsUserPresent_False() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean isPresent = usersService.isUserPresent(email);
        assertFalse(isPresent);
    }

    // Test for isPasswordMatching()
    @Test
    void testIsPasswordMatching_Success() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        User user = new User();
        user.setPassword(encodedPassword);

        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        boolean result = usersService.isPasswordMatching(user, user);
        assertTrue(result);
    }

    @Test
    void testIsPasswordMatching_Failure() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        User user = new User();
        user.setPassword(encodedPassword);

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        boolean result = usersService.isPasswordMatching(user, user);
        assertFalse(result);
    }

    // Test for saveUser()
    @Test
    void testSaveUser() {
        User user = new User();
        usersService.saveUser(user);
        verify(userRepository, times(1)).save(user);
    }

    // Test for createUser()
    @Test
    void testCreateUser() {
        UserAuthDto userAuthDto = new UserAuthDto("testuser", "testuser@example.com", "password");
        User user = new User();
        user.setEmail(userAuthDto.getEmail());
        when(convertorUtil.convertUserAuthDtoToUser(userAuthDto)).thenReturn(user);
        when(passwordEncoder.encode(userAuthDto.getPassword())).thenReturn("encodedPassword");

        usersService.createUser(userAuthDto);

        verify(passwordEncoder, times(1)).encode(userAuthDto.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    // Test for getUserJWT()
    @Test
    void testGetUserJWT_Success() {
        UserAuthDto userAuthDto = new UserAuthDto("testuser", "testuser@example.com", "password");
        User user = new User();
        user.setEmail("testuser@example.com");
        when(convertorUtil.convertUserAuthDtoToUser(userAuthDto)).thenReturn(user);

        User existingUser = new User();
        existingUser.setEmail("testuser@example.com");
        existingUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken("testuser@example.com")).thenReturn("generatedJwt");

        String jwtToken = usersService.getUserJWT(userAuthDto);
        assertEquals("generatedJwt", jwtToken);
    }

    @Test
    void testGetUserJWT_InvalidPassword() {
        UserAuthDto userAuthDto = new UserAuthDto("testuser", "testuser@example.com", "password");
        User user = new User();
        user.setEmail("testuser@example.com");

        User existingUser = new User();
        existingUser.setEmail("testuser@example.com");
        existingUser.setPassword("encodedPassword");

        when(convertorUtil.convertUserAuthDtoToUser(any())).thenReturn(user);
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        String jwtToken = usersService.getUserJWT(userAuthDto);
        assertEquals("", jwtToken);
    }

    // Test for deleteUserByEmail()
    @Test
    void testDeleteUserByEmail() {
        String email = "testuser@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        usersService.deleteUserByEmail(email);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteUserByEmail_NotFound() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        usersService.deleteUserByEmail(email);
        verify(userRepository, never()).delete(any());
    }
}