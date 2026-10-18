package com.example.msfeed.service;

import com.example.msfeed.dto.BatchInteractionRequest;
import com.example.msfeed.entity.Interaction;
import com.example.msfeed.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;

    public void processBatchInteractions(UUID userId, BatchInteractionRequest request) {
        Instant now = Instant.now();

        List<Interaction> entities = request.getInteractions().stream()
                .map(item -> Interaction.builder()
                        .userId(userId)
                        .pinId(item.getPinId())
                        .interactionType(item.getInteractionType())
                        .metadata(item.getMetadata())
                        .createdAt(item.getTimestamp() != null ? item.getTimestamp() : now)
                        .build())
                .toList();

        interactionRepository.saveAll(entities);
    }
}