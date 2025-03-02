package com.assessment.speernotes.repository;

import com.assessment.speernotes.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotesRepository extends MongoRepository<Note, String> {
    // Full-text search query with MongoDB Indexing
    @Query("{'$text': {'$search': ?0}}")
    List<Note> searchNotes(String keyword);
    List<Note> findByUserId(String userId);
}
