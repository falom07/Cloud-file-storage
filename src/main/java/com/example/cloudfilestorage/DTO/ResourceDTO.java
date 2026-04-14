package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;
import com.example.cloudfilestorage.Entity.User;

import java.time.LocalDateTime;

public record ResourceDTO (String path, String name, Long size,
                           ResourceType type, User owner, LocalDateTime createdAt){
}
