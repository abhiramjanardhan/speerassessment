package com.assessment.speernotes.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notes")
@Data
public class Note {
    @Id
    private String id;
    private String userId;
    @TextIndexed
    private String title;
    @TextIndexed
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
}
