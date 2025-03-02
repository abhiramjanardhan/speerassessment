package com.assessment.speernotes.service;

import com.assessment.speernotes.exceptions.NotesException;
import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.repository.NotesRepository;
import com.assessment.speernotes.utils.ConvertorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotesServiceTest {

    @InjectMocks
    private NotesService notesService;

    @Mock
    private NotesRepository notesRepository;

    @Mock
    private UsersService usersService;

    @Mock
    private ConvertorUtil convertorUtil;

    @Mock
    private UserDetails userDetails;

    private User authenticatedUser;
    private Note note;
    private NoteDto noteDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticatedUser = new User();
        authenticatedUser.setId("1");
        authenticatedUser.setEmail("testuser@example.com");

        note = new Note();
        note.setId("note1");
        note.setUserId(authenticatedUser.getId());
        note.setTitle("Test Note");
        note.setContent("This is a test note.");
        note.setCreatedAt(Instant.now());
        note.setUpdatedAt(Instant.now());

        noteDto = new NoteDto("Test Note", "This is a test note.");
    }

    @Test
    void testGetAllNotes() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.findByUserId(authenticatedUser.getId())).thenReturn(List.of(note));

        var notes = notesService.getAllNotes();

        assertEquals(1, notes.size());
        assertEquals(note, notes.get(0));
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findByUserId(authenticatedUser.getId());
    }

    @Test
    void testGetNoteById() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.findById("note1")).thenReturn(Optional.of(note));

        var foundNote = notesService.getNoteById("note1");

        assertTrue(foundNote.isPresent());
        assertEquals(note, foundNote.get());
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testCreateNote() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(convertorUtil.convertNoteDtoToNote(noteDto)).thenReturn(note);
        when(notesRepository.save(note)).thenReturn(note);

        var createdNote = notesService.createNote(noteDto);

        assertNotNull(createdNote);
        assertEquals(note.getTitle(), createdNote.getTitle());
        assertEquals(note.getContent(), createdNote.getContent());
        assertEquals(authenticatedUser.getId(), createdNote.getUserId());
        verify(usersService).getAuthenticatedUser();
        verify(convertorUtil).convertNoteDtoToNote(noteDto);
        verify(notesRepository).save(note);
    }

    @Test
    void testUpdateNote() {
        when(convertorUtil.convertNoteDtoToNote(any())).thenReturn(note);
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.findById("note1")).thenReturn(Optional.of(note));
        when(notesRepository.save(note)).thenReturn(note);

        var updatedNote = notesService.updateNote("note1", noteDto);

        assertTrue(updatedNote.isPresent());
        assertEquals(noteDto.getTitle(), updatedNote.get().getTitle());
        assertEquals(noteDto.getContent(), updatedNote.get().getContent());
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
        verify(notesRepository).save(note);
    }

    @Test
    void testUpdateNote_NoteNotBelongToUser() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Note otherNote = new Note();
        otherNote.setUserId("2");
        when(notesRepository.findById("note1")).thenReturn(Optional.of(otherNote));

        NotesException thrown = assertThrows(NotesException.class, () -> {
            notesService.updateNote("note1", noteDto);
        });

        assertEquals("Invalid note. Please try again! Reason: The note does not belong to the user!", thrown.getMessage());
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testDeleteNote() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.findById("note1")).thenReturn(Optional.of(note));

        notesService.deleteNote("note1");

        verify(notesRepository).delete(note);
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testDeleteNote_NoteNotBelongToUser() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Note otherNote = new Note();
        otherNote.setUserId("2");
        when(notesRepository.findById("note1")).thenReturn(Optional.of(otherNote));

        NotesException thrown = assertThrows(NotesException.class, () -> {
            notesService.deleteNote("note1");
        });

        assertEquals("Invalid note. Please try again! Reason: The note does not belong to the user!", thrown.getMessage());
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testShareNoteToAnotherUser() {
        String email = "shareduser@example.com";
        when(usersService.findUserByEmail(email)).thenReturn(authenticatedUser);
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.findById("note1")).thenReturn(Optional.of(note));

        boolean status = notesService.shareNoteToAnotherUser("note1", email);

        assertTrue(status);
        verify(usersService).findUserByEmail(email);
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testShareNoteToAnotherUser_NoteNotBelongToUser() {
        String email = "shareduser@example.com";
        when(usersService.findUserByEmail(email)).thenReturn(authenticatedUser);
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Note otherNote = new Note();
        otherNote.setUserId("2");
        when(notesRepository.findById("note1")).thenReturn(Optional.of(otherNote));

        boolean status = notesService.shareNoteToAnotherUser("note1", email);

        assertFalse(status);
        verify(usersService).findUserByEmail(email);
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).findById("note1");
    }

    @Test
    void testSearchNoteForQuery() {
        when(usersService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(notesRepository.searchNotes("test")).thenReturn(List.of(note));

        var result = notesService.searchNoteForQuery("test");

        assertFalse(result.isEmpty());
        assertEquals(note, result.get(0));
        verify(usersService).getAuthenticatedUser();
        verify(notesRepository).searchNotes("test");
    }
}