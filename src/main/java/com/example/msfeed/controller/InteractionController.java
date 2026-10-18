package com.example.msfeed.controller;

import com.example.msfeed.dto.BatchInteractionRequest;
import com.example.msfeed.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    public ResponseEntity<Void> recordBatchInteractions(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BatchInteractionRequest request) {

        interactionService.processBatchInteractions(UUID.fromString(Objects.requireNonNull(jwt.getSubject())), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}