package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.ErrorResponse;
import com.example.cloudfilestorage.DTO.UserResponseDTO;
import com.example.cloudfilestorage.Exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Інформація про користувача")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Отримати username діючого користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Імя користувача успішно знайдено",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Map<String, String>> getUser(Authentication auth) {
        String username = auth.getName();
        if (username.isEmpty()) throw new UnauthorizedException();

        return ResponseEntity.ok(Map.of("username", username));
    }
}
