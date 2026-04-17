package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;

public record DirectoryDTO(String path, String name, ResourceType type) {
}
