package com.assessment.speernotes.requests;

import com.assessment.speernotes.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthentication jwtAuthentication;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jwtAuthentication).addFilters((OncePerRequestFilter) jwtAuthentication).build();
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Mocking the behavior of JwtUtil
        String token = "valid-jwt-token";
        String email = "user@example.com";
        UserDetails userDetails = User.builder().username(email).password("password").authorities("ROLE_USER").build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // Call the method
        jwtAuthentication.doFilterInternal(request, response, filterChain);

        // Verify that the SecurityContext contains the authentication
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());

        // Verify that the filter chain is called
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Mocking the behavior of JwtUtil
        String token = "invalid-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Call the method
        jwtAuthentication.doFilterInternal(request, response, filterChain);

        // Verify that the SecurityContext does not contain authentication
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify that the filter chain is called
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testGetJwtFromRequest_NoToken_ReturnsNull() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Get the private method using reflection
        Method method = JwtAuthentication.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);

        // Mocking the request behavior
        when(request.getHeader("Authorization")).thenReturn(null);

        // Invoke the private method
        String token = (String) method.invoke(jwtAuthentication, request);

        // Assert
        assertNull(token);
    }

    @Test
    void testGetJwtFromRequest_InvalidTokenFormat_ReturnsNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get the private method using reflection
        Method method = JwtAuthentication.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);

        // Mocking the request behavior
        when(request.getHeader("Authorization")).thenReturn("BearerInvalidToken");

        // Invoke the private method
        String token = (String) method.invoke(jwtAuthentication, request);

        // Assert
        assertNull(token);
    }

    @Test
    void testGetJwtFromRequest_ValidToken_ReturnsToken() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get the private method using reflection
        Method method = JwtAuthentication.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);

        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Invoke the private method
        String extractedToken = (String) method.invoke(jwtAuthentication, request);

        // Assert
        assertEquals(token, extractedToken);
    }
}