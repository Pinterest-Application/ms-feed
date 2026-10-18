package com.example.msfeed.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInteractionRequest {

    @NotEmpty(message = "Interactions list cannot be empty")
    @Size(max = 50, message = "Batch size cannot exceed 50")
    @Valid
    private List<InteractionItemRequest> interactions;
}