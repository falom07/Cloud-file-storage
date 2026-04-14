package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;

public record FileDTO(String path, String name, Long size, ResourceType type) {
}
