package com.example.msfeed.repository;

import com.example.msfeed.entity.Interaction;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends MongoRepository<Interaction, String> {

    @Query(value = "{ 'userId': ?0 }",
            fields = "{ 'pinId': 1, 'interactionType': 1, 'createdAt': 1 }",
            sort = "{ 'createdAt': -1 }")
    List<Interaction> findRecentByUserId(UUID userId, Limit limit);
}
