package com.assessment.speernotes.requests;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitInterceptorTest {

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;  // Interceptor to test

    @Mock
    private MockHttpServletRequest request;  // Mock the HttpServletRequest

    @Mock
    private MockHttpServletResponse response; // Mock the HttpServletResponse

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    @Test
    void testRateLimitingEnabled() throws Exception {
        // Set rate-limiting enabled to true
        rateLimitInterceptor.rateLimitingEnabled = true;

        // Prepare the response to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(printWriter); // Mocking the response's writer

        // Mock the request to return a specific IP
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        // Simulate 5 requests (should all pass)
        for (int i = 0; i < 5; i++) {
            boolean result = rateLimitInterceptor.preHandle(request, response, null);
            assertTrue(result);  // All requests should pass in this case
        }

        // Ensure no throttling has happened yet
        verify(response, never()).addHeader(eq("X-RateLimit-Throttled"), eq("true"));
    }

    @Test
    void testExcessiveRequestsBlocked() throws Exception {
        // Set rate-limiting enabled
        rateLimitInterceptor.rateLimitingEnabled = true;

        // Prepare the response to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(printWriter);  // Mocking the response's writer

        // Mock the request to return a specific IP
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        // Simulate MAX REQUESTS requests (should all pass)
        for (int i = 0; i < RateLimitInterceptor.MAX_REQUESTS * 2; i++) {
            boolean result = rateLimitInterceptor.preHandle(request, response, null);
            assertTrue(result);  // First 5 requests should pass
        }

        // Simulate 1 more request (should be throttled or blocked)
        boolean resultAfterLimit = rateLimitInterceptor.preHandle(request, response, null);
        assertFalse(resultAfterLimit);  // After 5 requests, it should block the 6th request

        // Check the response status is 416 for blocked requests
        verify(response).setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);

        // Check the response content
        printWriter.flush();  // Ensure the content is written to the output stream
        String responseContent = outputStream.toString().trim();
        assertEquals("Too many requests. Try again later.", responseContent);  // Expecting the error message
    }

    @Test
    void testTimeWindowReset() throws Exception {
        // Set rate-limiting enabled to true
        rateLimitInterceptor.rateLimitingEnabled = true;

        // Prepare the response to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(printWriter); // Mocking the response's writer

        // Mock the request to return a specific IP
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        // Simulate 5 requests (all should pass)
        for (int i = 0; i < 5; i++) {
            boolean result = rateLimitInterceptor.preHandle(request, response, null);
            assertTrue(result);  // First 5 requests should pass
        }

        // Now simulate a request after the time window has reset
        Thread.sleep(60 * 1000); // Wait for 1 minute to reset the window

        boolean resultAfterReset = rateLimitInterceptor.preHandle(request, response, null);
        assertTrue(resultAfterReset);  // After the time window is reset, the request should pass again
    }
}