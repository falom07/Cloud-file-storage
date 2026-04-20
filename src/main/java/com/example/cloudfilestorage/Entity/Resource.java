package com.example.cloudfilestorage.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String name;

    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Resource(String path, String name, Long size, ResourceType type, User owner) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.type = type;
        this.owner = owner;
    }
}
