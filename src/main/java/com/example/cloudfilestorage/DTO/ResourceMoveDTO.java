package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;

public record ResourceMoveDTO(String path, String name, Long size, ResourceType type)  {
}
