package com.example.cloudfilestorage.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обєк для зберігання auth даних користувача")
public record UserDTO(@Schema(description = "Імя користувача", example = "Spange_Bob") String username,
                      @Schema(description = "Пароль користувача", example = "12345") String password) {
}
