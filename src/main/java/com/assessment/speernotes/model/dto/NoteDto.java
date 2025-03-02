package com.assessment.speernotes.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteDto {
    @NotNull
    private String title;
    @NotNull
    private String content;

    public NoteDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
