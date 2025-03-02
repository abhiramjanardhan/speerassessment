package com.assessment.speernotes.service;

import com.assessment.speernotes.exceptions.NotesException;
import com.assessment.speernotes.exceptions.UserException;
import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.repository.NotesRepository;
import com.assessment.speernotes.utils.ConvertorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotesService {
    private final NotesRepository noteRepository;
    private final UsersService usersService;
    private final ConvertorUtil convertorUtil;

    public NotesService(NotesRepository noteRepository, UsersService usersService, ConvertorUtil convertorUtil) {
        this.noteRepository = noteRepository;
        this.usersService = usersService;
        this.convertorUtil = convertorUtil;
    }

    /**
     * This method is used to return the current authenticated user
     *
     * @return User
     */
    private User getAuthenticatedUser() {
        return usersService.getAuthenticatedUser();
    }

    /**
     * This method is used to get all the user notes
     *
     * @param user
     * @return List<Note>
     */
    public List<Note> getAllUserNotes(User user) {
        return noteRepository.findByUserId(user.getId());
    }

    /**
     * This method is used to retrieve all the notes associated with the current authenticated user
     *
     * @return List<Note>
     */
    public List<Note> getAllNotes() {
        User user = getAuthenticatedUser();
        return getAllUserNotes(user);
    }

    /**
     * This method is used to retrieve the note by the note id associated with the current authenticated user
     *
     * @param id
     * @return Optional<Note>
     */
    public Optional<Note> getNoteById(String id) {
        User user = getAuthenticatedUser();
        return noteRepository.findById(id).filter(note -> note.getUserId().equals(user.getId()));
    }

    /**
     * This method is used to create the note associated with the current authenticated user
     *
     * @param noteDto
     * @return Note
     */
    public Note createNote(NoteDto noteDto) {
        Note note = convertorUtil.convertNoteDtoToNote(noteDto);
        User user = getAuthenticatedUser();
        note.setUserId(user.getId());
        note.setCreatedAt(Instant.now());
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    /**
     * This method is used to update the note for the note id associated with the current authenticated user
     *
     * @param noteId
     * @param noteDto
     * @return Optional<Note>
     */
    public Optional<Note> updateNote(String noteId, NoteDto noteDto) {
        Note newNote = convertorUtil.convertNoteDtoToNote(noteDto);
        User user = getAuthenticatedUser();

        return noteRepository.findById(noteId).map(note -> {
            if (!note.getUserId().equals(user.getId())) {
                throw new NotesException("The note does not belong to the user!");
            }

            if (Optional.ofNullable(newNote.getTitle()).isPresent()) {
                note.setTitle(newNote.getTitle());
            }

            if (Optional.ofNullable(newNote.getContent()).isPresent()) {
                note.setContent(newNote.getContent());
            }

            note.setUpdatedAt(Instant.now());
            return noteRepository.save(note);
        });
    }

    /**
     * This method is used to delete the note
     *
     * @param note
     */
    public void deleteUserNote(Note note) {
        noteRepository.delete(note);
    }

    /**
     * This method is used to delete the note for the id associated with the current authenticated user
     *
     * @param noteId
     */
    public void deleteNote(String noteId) {
        User user = getAuthenticatedUser();
        noteRepository.findById(noteId).ifPresent(note -> {
            if (note.getUserId().equals(user.getId())) {
                deleteUserNote(note);
            } else {
                throw new NotesException("The note does not belong to the user!");
            }
        });
    }

    /**
     * This method is used to share the note associated with the current authenticated user with another user
     *
     * @param noteId
     * @param email
     * @return boolean
     */
    public boolean shareNoteToAnotherUser(String noteId, String email) {
        boolean status = false;

        try {
            User sharedUser = usersService.findUserByEmail(email);
            User user = getAuthenticatedUser();

            status = noteRepository.findById(noteId).map(note -> {
                if (note.getUserId().equals(user.getId())) {
                    sharedUser.getSharedNotes().add(noteId);
                    usersService.saveUser(sharedUser);
                    return true;
                }
                return false;
            }).orElse(false);
            log.info(String.valueOf(status));
            log.info(sharedUser.getSharedNotes().toString());
        } catch (UserException e) {
            throw new NotesException("Notes cannot be shared to the user " + email + " as the user is invalid!");
        } catch (Exception ignored) {}

        return status;
    }

    /**
     * This method is used to get all the available notes which satisfies the query associated with the current authenticated user
     *
     * @param query
     * @return List<Note>
     */
    public List<Note> searchNoteForQuery(String query) {
        User user = getAuthenticatedUser();
        return noteRepository.searchNotes(query).stream()
                .filter(note -> note.getUserId().equals(user.getId()) || user.getSharedNotes().contains(note.getId()))
                .toList();
    }
}
