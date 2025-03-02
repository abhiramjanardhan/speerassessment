package com.assessment.speernotes.controller;

import com.assessment.speernotes.exceptions.UserException;
import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.service.NotesService;
import com.assessment.speernotes.service.UsersService;
import com.assessment.speernotes.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotesControllerE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsersService usersService;

    @Autowired
    private NotesService notesService;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    private String validJwtToken;

    private User user;

    @BeforeEach
    void setUp() {
        try {
            user = usersService.findUserByEmail("e2etestuser@example.com");
        } catch (UserException e) {
            usersService.createUser(new UserAuthDto("e2etestuser", "e2etestuser@example.com", "password123"));
            user = usersService.findUserByEmail("e2etestuser@example.com");
        }

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        validJwtToken = jwtUtil.generateToken(user.getEmail());

        // Manually set authentication in the SecurityContext
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("e2etestuser@example.com", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    public void cleanup() {
        List<Note> notes = notesService.getAllUserNotes(user);
        notes.forEach(note -> notesService.deleteUserNote(note));
    }

    /**
     * Test for creating a note with a valid JWT token
     */
    @Test
    void testCreateNoteWithValidJwt() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken) // Passing valid JWT token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test Title\", \"content\": \"Test Content\"}"))
                .andExpect(status().isCreated())  // Check for 201 Created
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    /**
     * Test for getting all notes with a valid JWT token
     */
    @Test
    void testGetAllNotesWithValidJwt() throws Exception {
        mockMvc.perform(post("/api/notes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken) // Passing valid JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Test Title\", \"content\": \"Test Content\"}"));

        mockMvc.perform(get("/api/notes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    /**
     * Test for getting a note by id with a valid JWT token
     */
    @Test
    void testGetNoteByIdWithValidJwt() throws Exception {
        // Create a note for testing
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUserId("e2etestuser@example.com");
        note.setCreatedAt(Instant.now());
        note.setUpdatedAt(Instant.now());

        Note savedNote = notesService.createNote(new NoteDto("Test Note", "Test Content"));

        mockMvc.perform(get("/api/notes/" + savedNote.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedNote.getId()))
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    /**
     * Test for searching notes with a valid JWT token
     */
    @Test
    void testSearchNotesWithValidJwt() throws Exception {
        mockMvc.perform(post("/api/notes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken) // Passing valid JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Test Title\", \"content\": \"Test Content\"}"));

        mockMvc.perform(get("/api/notes/search")
                        .param("query", "Test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").isNotEmpty());
    }

    /**
     * Test for deleting a note with a valid JWT token
     */
    @Test
    void testDeleteNoteWithValidJwt() throws Exception {
        Note note = notesService.createNote(new NoteDto("Test Title", "Test Content"));

        mockMvc.perform(delete("/api/notes/{id}", note.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken))
                .andExpect(status().isOk());
    }
}