package com.example.msfeed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryFeedPage {

    private List<FeedResponse> items;
    private String nextCursor;
    private boolean hasMore;
}
