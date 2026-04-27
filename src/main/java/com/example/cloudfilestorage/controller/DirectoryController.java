package com.example.cloudfilestorage.controller;

import com.example.cloudfilestorage.dto.ErrorResponse;
import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.service.ResourceService;
import com.example.cloudfilestorage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Tag(name = "Directory", description = "Управління дерикторіями")
public class DirectoryController {

    private final ResourceService resourceService;
    private final StorageService storageService;

    @Operation(summary = "Взяти дерикторії")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс Створено",
                    content = @Content(schema = @Schema(implementation = ResourceDTO.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях до дерикторії",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Відстуня батьківська дерикторія ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Така дерикторія вже існує",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getInfo(
            @Parameter(description = "Шлях до директорії в які ми хочемо дізнатися інформацію про ресурси", example = "main2/src2/prod/")
            @RequestParam("path") String path, Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<ResourceDTO> list = resourceService.getInfoAboutDirectory(path, user.getId());

        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Створити дерикторію")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс Створено",
                    content = @Content(schema = @Schema(implementation = ResourceDTO.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях до дерикторії",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Відстуня батьківська дерикторія ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Така дерикторія вже існує",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ResourceDTO> createDirectory(
            @Parameter(description = "Шлях до директорії та її назва", example = "main2/src2/prod/") @RequestParam("path") String path, Authentication auth) {
        User user = (User) auth.getPrincipal();
        ResourceDTO directory = resourceService.createDirectories(path, user.getId());
        storageService.createDirectory(path, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }

}
