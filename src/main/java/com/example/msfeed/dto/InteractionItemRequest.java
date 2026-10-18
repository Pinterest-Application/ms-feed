package com.example.msfeed.dto;

import com.example.msfeed.entity.InteractionMetadata;
import com.example.msfeed.entity.enums.InteractionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionItemRequest {

    @NotNull(message = "pinId cannot be null")
    private UUID pinId;

    @NotNull(message = "interactionType cannot be null")
    private InteractionType interactionType;

    private InteractionMetadata metadata;

    private Instant timestamp;
}