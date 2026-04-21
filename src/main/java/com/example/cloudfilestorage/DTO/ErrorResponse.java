package com.example.cloudfilestorage.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Інформація про помилку")
public record ErrorResponse(@Schema(description = "Повідомлення помилки", example = "error message") String message) {
}
