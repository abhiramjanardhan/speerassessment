package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.service.NotesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("api/notes")
@Tag(name = "Notes End Points", description = "The end points to do the CRUD operations on Notes")
public class NotesController {
    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    /**
     * This end point is used to get all the notes associated with the user
     *
     * @return ResponseEntity<List<Note>>
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point gets all the notes associated with the user")
    public ResponseEntity<List<Note>> getAllNotes() {
        log.info("GET /api/notes");
        return ResponseEntity.ok(notesService.getAllNotes());
    }

    /**
     * This end point is used to get the note based on the note id for the user
     *
     * @param id
     * @return ResponseEntity<Optional<Note>>
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point is used to get the note by id")
    public ResponseEntity<Optional<Note>> getNote(@PathVariable String id) {
        log.info("GET /api/notes/id {}", id);
        return ResponseEntity.ok(notesService.getNoteById(id));
    }

    /**
     * This end point is used to create the note for the user
     *
     * @param noteDto
     * @return ResponseEntity<Note>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "This end point is used to create the note")
    public ResponseEntity<Note> createNote(@RequestBody @Valid NoteDto noteDto) {
        log.info("POST /api/notes {}, {}", noteDto.getTitle(), noteDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(notesService.createNote(noteDto));
    }

    /**
     * This end point is used to update the note for the user
     *
     * @param id
     * @param noteDto
     * @return ResponseEntity<Optional<Note>>
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point is used to update the existing note by id")
    public ResponseEntity<Optional<Note>> updateNote(@PathVariable String id, @RequestBody NoteDto noteDto) {
        log.info("PUT /api/notes/{}, {}, {}", id, noteDto.getTitle(), noteDto.getContent());
        return ResponseEntity.ok(notesService.updateNote(id, noteDto));
    }

    /**
     * This end point is used to delete the note for the user
     *
     * @param id
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point is used to delete the existing note by id")
    public ResponseEntity<String> deleteNote(@PathVariable String id) {
        log.info("DELETE /api/notes/{}", id);
        notesService.deleteNote(id);
        return ResponseEntity.ok("Note " + id + " is deleted successfully.");
    }

    /**
     * This end point is used to share the note to another user
     *
     * @param id
     * @param email
     * @return ResponseEntity<String>
     */
    @PostMapping("/{id}/share")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point is used to share the note with another user")
    public ResponseEntity<String> shareNote(@PathVariable String id, @RequestParam String email) {
        log.info("POST /api/notes/{}/share {}", id, email);
        boolean status = notesService.shareNoteToAnotherUser(id, email);
        return status
                ? ResponseEntity.ok("Note having the id " + id + " shared successfully to the email: " + email)
                : ResponseEntity.ok("Failed to share note for the id " + id);
    }

    /**
     * This end point is used to query the notes based on the query string
     *
     * @param query
     * @return ResponseEntity<List<Note>>
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "This end point is used to query the notes based on the keywords")
    public ResponseEntity<List<Note>> searchNotes(@RequestParam String query) {
        log.info("GET /api/notes/search {}", query);
        return ResponseEntity.ok(notesService.searchNoteForQuery(query));
    }
}
