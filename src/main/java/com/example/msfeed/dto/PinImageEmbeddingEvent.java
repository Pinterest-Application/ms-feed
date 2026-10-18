package com.example.msfeed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinImageEmbeddingEvent {
    private UUID pinId;
    private String mediaUrl;
    private Integer mediaWidth;
    private Integer mediaHeight;
    private float[] embedding;
}
