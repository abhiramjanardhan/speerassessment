package com.assessment.speernotes.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteDto {
    private String userId;
    @NotNull
    private String title;
    @NotNull
    private String content;
}
