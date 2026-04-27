package com.example.cloudfilestorage.dto;

import com.example.cloudfilestorage.entity.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обєкт для зберігання мета данних файлу")
public record ResourceDTO(
        @Schema(description = "Шлях до файлу", example = "/main/src/prod/") String path,
        @Schema(description = "Імя файлу", example = "text.txt") String name,
        @Schema(description = "Розмір файлу",  example = "154") @JsonInclude(JsonInclude.Include.NON_NULL) Long size,
        @Schema(description = "Тип ресурсу", example = "FILE") ResourceType type) {
}