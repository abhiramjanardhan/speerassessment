package com.assessment.speernotes.utils;

import com.assessment.speernotes.model.Note;
import com.assessment.speernotes.model.dto.NoteDto;
import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.dto.UserAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConvertorUtil {
    @Autowired
    private ModelMapper modelMapper;

    /**
     * This method is used to convert user auth dto object to user object
     *
     * @param userAuthDto
     * @return User
     */
    public User convertUserAuthDtoToUser(UserAuthDto userAuthDto) {
        log.info(String.valueOf(userAuthDto));
        return modelMapper.map(userAuthDto, User.class);
    }

    /**
     * This method is used to convert the user object to user auth dto object
     *
     * @param user
     * @return UserAuthDto
     */
    public UserAuthDto convertUserToUserAuth(User user) {
        return modelMapper.map(user, UserAuthDto.class);
    }

    /**
     * This method is used to convert the note dto object to note object
     *
     * @param noteDto
     * @return Note
     */
    public Note convertNoteDtoToNote(NoteDto noteDto) {
        return modelMapper.map(noteDto, Note.class);
    }

    /**
     * This method is used to convert the note object to note dto object
     *
     * @param note
     * @return NoteDto
     */
    public NoteDto convertNoteToNoteDto(Note note) {
        return modelMapper.map(note, NoteDto.class);
    }
}
