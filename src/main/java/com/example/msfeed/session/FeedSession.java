package com.example.msfeed.session;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FeedSession {

    private final UUID id;
    private final UUID userId;
    private int size;
    private boolean exhausted;

    public FeedSession(UUID id, UUID userId, int size, boolean exhausted) {
        this.id = id;
        this.userId = userId;
        this.size = size;
        this.exhausted = exhausted;
    }

    void setSize(int size) {
        this.size = size;
    }

    void setExhausted() {
        this.exhausted = true;
    }
}
