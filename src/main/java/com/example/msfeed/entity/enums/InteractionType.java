package com.example.msfeed.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InteractionType {

    SAVE(1.0, InteractionCategory.EXPLICIT_POSITIVE),
    FOLLOW_AUTHOR(0.9, InteractionCategory.EXPLICIT_POSITIVE),
    SHARE(0.8, InteractionCategory.EXPLICIT_POSITIVE),
    COMMENT(0.7, InteractionCategory.EXPLICIT_POSITIVE),
    LINK_CLICK(0.6, InteractionCategory.EXPLICIT_POSITIVE),
    LIKE(0.6, InteractionCategory.EXPLICIT_POSITIVE),

    CLICK_EXPAND(0.4, InteractionCategory.PASSIVE_POSITIVE),
    LONG_DWELL(0.3, InteractionCategory.PASSIVE_POSITIVE),
    SCROLL_PAUSE(0.15, InteractionCategory.PASSIVE_POSITIVE),

    IMPRESSION_SKIP(-0.05, InteractionCategory.IMPLICIT_NEGATIVE),
    QUICK_BOUNCE(-0.3, InteractionCategory.IMPLICIT_NEGATIVE),

    HIDE_PIN(-1.0, InteractionCategory.EXPLICIT_NEGATIVE),
    FEEDBACK_NOT_RELEVANT(-1.0, InteractionCategory.EXPLICIT_NEGATIVE),
    FEEDBACK_SEEN_OFTEN(-0.4, InteractionCategory.EXPLICIT_NEGATIVE);

    private final double weight;
    private final InteractionCategory category;

    public enum InteractionCategory {
        EXPLICIT_POSITIVE,
        PASSIVE_POSITIVE,
        IMPLICIT_NEGATIVE,
        EXPLICIT_NEGATIVE
    }
}
