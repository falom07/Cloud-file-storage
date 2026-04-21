package com.example.cloudfilestorage.DTO;

import com.example.cloudfilestorage.Entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обєкт для зберігання мета даних файлу")
public record FileDTO(
        @Schema(description = "Шлях до файлу", example = "/main/src/prod/") String path,
        @Schema(description = "Імя файлу", example = "text.txt") String name,
        @Schema(description = "Розмір файлу", example = "154") Long size,
        @Schema(description = "Тип ресурсу", example = "FILE") ResourceType type) implements ResourceDTO {
}
