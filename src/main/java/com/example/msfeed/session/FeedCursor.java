package com.example.msfeed.session;

import com.example.libexception.exception.BadRequestException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class FeedCursor {

    private static final String INVALID_CURSOR = "Invalid cursor";

    private final UUID sessionId;
    private final int offset;

    public String encode() {
        String raw = sessionId + ":" + offset;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FeedCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separator = raw.lastIndexOf(':');
            UUID sessionId = UUID.fromString(raw.substring(0, separator));
            int offset = Integer.parseInt(raw.substring(separator + 1));
            if (offset < 0) {
                throw new BadRequestException(INVALID_CURSOR);
            }
            return new FeedCursor(sessionId, offset);
        } catch (BadRequestException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BadRequestException(INVALID_CURSOR);
        }
    }
}
