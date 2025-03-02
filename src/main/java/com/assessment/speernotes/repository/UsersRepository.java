package com.assessment.speernotes.repository;

import com.assessment.speernotes.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends MongoRepository<User, String> {
    @Query("{ 'email': ?0 }")
    Optional<User> findByEmail(String email);
}
