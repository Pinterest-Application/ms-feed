package com.example.msfeed.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "feed", schema = "ms_feed")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pin_id", nullable = false, unique = true)
    private UUID pinId;

    @Column(name = "media_url", nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "media_width")
    private Integer mediaWidth;

    @Column(name = "media_height")
    private Integer mediaHeight;

    @Column(name = "embedding", columnDefinition = "vector(1152)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1152)
    private float[] embedding;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}