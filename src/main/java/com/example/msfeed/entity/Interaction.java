package com.example.msfeed.entity;

import com.example.msfeed.entity.enums.InteractionType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "interactions")
@CompoundIndexes({
        @CompoundIndex(name = "user_pin_idx", def = "{'userId': 1, 'pinId': 1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    private String id;

    @Field("userId")
    private UUID userId;

    @Field("pinId")
    private UUID pinId;

    @Field("interactionType")
    private InteractionType interactionType;

    @Field("metadata")
    private InteractionMetadata metadata;

    @CreatedDate
    @Indexed(name = "interaction_ttl_idx", expireAfter = "30d")
    @Field("createdAt")
    private Instant createdAt;

    @Transient
    public double getWeight() {
        return interactionType != null ? interactionType.getWeight() : 0.0;
    }
}