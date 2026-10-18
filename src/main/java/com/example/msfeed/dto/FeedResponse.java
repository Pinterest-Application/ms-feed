package com.example.msfeed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {
    private UUID id;
    private UUID pinId;
    private String mediaUrl;
    private Integer mediaWidth;
    private Integer mediaHeight;
    private OffsetDateTime createdAt;
}