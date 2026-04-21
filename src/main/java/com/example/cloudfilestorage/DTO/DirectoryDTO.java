package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обєкт для зберігання мета даних директорій")
public record DirectoryDTO(
        @Schema(description = "Шлях до директорії", example = "/main/src/") String path,
        @Schema(description = "Імя директорії", example = "prod") String name,
        @Schema(description = "Тип ресурсу", example = "DIRECTORY")ResourceType type) implements ResourceDTO{
}
