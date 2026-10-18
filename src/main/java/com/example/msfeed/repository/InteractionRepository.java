package com.example.msfeed.repository;

import com.example.msfeed.entity.Interaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends MongoRepository<Interaction, String> {
    List<Interaction> findByUserId(UUID userId);
}