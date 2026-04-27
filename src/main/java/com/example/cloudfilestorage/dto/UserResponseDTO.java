package com.example.cloudfilestorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Імя користувача")
public record UserResponseDTO(@Schema(description = "Імя користувача", example = "Spange_Bob") String username) {
}
