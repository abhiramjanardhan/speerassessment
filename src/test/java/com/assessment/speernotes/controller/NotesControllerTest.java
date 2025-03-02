package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.service.NotesService;
import com.assessment.speernotes.service.UsersService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotesControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersService usersService;

    @Autowired
    private NotesService notesService;

    private String jwtToken;
    private User loggedInUser;

    @BeforeEach
    public void setUp() {
        UserAuthDto userAuthDto = new UserAuthDto("testuser", "testuser@example.com", "password");
        UserAuthDto sharedUserAuthDto = new UserAuthDto("sharedtestuser", "sharedtestuser@example.com", "password");

        // Create the user if not present
        if (!usersService.isUserPresent("testuser@example.com")) {
            usersService.createUser(userAuthDto);
        }

        if (!usersService.isUserPresent("sharedtestuser@example.com")) {
            usersService.createUser(sharedUserAuthDto);
        }

        // Login to get the JWT token
        HttpEntity<UserAuthDto> request = new HttpEntity<>(userAuthDto);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);
        jwtToken = response.getBody();
        loggedInUser = usersService.findUserByEmail("testuser@example.com");
    }

    @AfterEach
    public void cleanup() {
        List<Note> notes = notesService.getAllUserNotes(loggedInUser);
        notes.forEach(note -> notesService.deleteUserNote(note));
    }

    @Test
    void testCreateNote() {
        NoteDto noteDto = new NoteDto("Test Note", "This is a test note");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);

        ResponseEntity<Note> response = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertEquals("Test Note", response.getBody().getTitle());
    }

    @Test
    void testGetAllNotes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange("/api/notes", HttpMethod.GET, request, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetNoteById() {
        // Create a note first
        NoteDto noteDto = new NoteDto("Test Note", "This is a test note");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);
        ResponseEntity<Note> createResponse = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        String noteId = createResponse.getBody().getId();

        // Now retrieve the note by id
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<Note> getResponse = restTemplate.exchange("/api/notes/{id}", HttpMethod.GET, getRequest, Note.class, noteId);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(noteId, getResponse.getBody().getId());
    }

    @Test
    void testUpdateNote() {
        // Create a note first
        NoteDto noteDto = new NoteDto("Test Note", "This is a test note");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);
        ResponseEntity<Note> createResponse = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        String noteId = createResponse.getBody().getId();
        NoteDto updatedNoteDto = new NoteDto("Updated Test Note", "This is an updated test note");

        // Update the note
        HttpEntity<NoteDto> updateRequest = new HttpEntity<>(updatedNoteDto, headers);
        ResponseEntity<Note> updateResponse = restTemplate.exchange("/api/notes/{id}", HttpMethod.PUT, updateRequest, Note.class, noteId);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Updated Test Note", updateResponse.getBody().getTitle());
    }

    @Test
    void testDeleteNote() {
        // Create a note first
        NoteDto noteDto = new NoteDto("Test Note", "This is a test note");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);
        ResponseEntity<Note> createResponse = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        String noteId = createResponse.getBody().getId();

        // Delete the note
        HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange("/api/notes/{id}", HttpMethod.DELETE, deleteRequest, String.class, noteId);

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertTrue(deleteResponse.getBody().contains("Note " + noteId + " is deleted successfully."));
    }

    @Test
    void testShareNote() {
        // Create a note first
        NoteDto noteDto = new NoteDto("Test Note", "This is a test note");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);
        ResponseEntity<Note> createResponse = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        String noteId = createResponse.getBody().getId();
        String email = "sharedtestuser@example.com"; // Assuming email is valid for testing sharing

        // Share the note
        HttpEntity<Void> shareRequest = new HttpEntity<>(headers);
        ResponseEntity<String> shareResponse = restTemplate.exchange("/api/notes/{id}/share?email={email}", HttpMethod.POST, shareRequest, String.class, noteId, email);

        assertEquals(HttpStatus.OK, shareResponse.getStatusCode());
        assertTrue(shareResponse.getBody().contains("shared successfully"));
    }

    @Test
    public void testInvalidShareNote() {
        // Create a note first
        NoteDto noteDto = new NoteDto("Shared Test Note", "This is a shared test note");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<NoteDto> request = new HttpEntity<>(noteDto, headers);
        ResponseEntity<Note> createResponse = restTemplate.exchange("/api/notes", HttpMethod.POST, request, Note.class);

        String noteId = createResponse.getBody().getId();
        String email = "sharedtestuser1@example.com"; // Assuming email is valid for testing sharing

        // Share the note
        HttpEntity<Void> shareRequest = new HttpEntity<>(headers);
        ResponseEntity<String> shareResponse = restTemplate.exchange("/api/notes/{id}/share?email={email}", HttpMethod.POST, shareRequest, String.class, noteId, email);

        assertEquals(HttpStatus.BAD_REQUEST, shareResponse.getStatusCode());
    }

    @Test
    void testSearchNotes() {
        String query = "test";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange("/api/notes/search?query=" + query, HttpMethod.GET, request, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
