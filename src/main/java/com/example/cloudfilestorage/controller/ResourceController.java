package com.example.cloudfilestorage.controller;

import com.example.cloudfilestorage.dto.ErrorResponse;
import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.service.FacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resource", description = "Керуванням файлами та дерикторіями")
public class ResourceController {

    private final FacadeService service;

    @Operation(summary = "Взяти ресурс по шляху")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ResourceDTO> getResource(
            @Parameter(description = "Шлях до ресурсу", example = "main/src/prod/text.txt") @RequestParam String path,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        ResourceDTO resourceDTO = service.get(path, user);

        return ResponseEntity.ok(resourceDTO);
    }

    @Operation(summary = "Видалення ресурсів")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(
            @Parameter(description = "Колекція шляхів за якими будуть видалені ресурси", example = "main/src/prod/")
            @RequestParam List<String> path, Authentication auth) {

        User user = (User) auth.getPrincipal();
        service.delete(path, user);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завантаження ресурсів")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceDTO>> uploadResource(
            @Parameter(description = "Шлях куди буде загружений ресурс", example = "main/src/prod/")
            @RequestParam("path") String path,
            Authentication auth,
            @Parameter(description = "Ресурси які будуть завантажені")
            @RequestPart("object") List<MultipartFile> files) {

        User user = (User) auth.getPrincipal();
        List<ResourceDTO> resourceDTO = service.upload(path, files, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDTO);
    }

    @Operation(summary = "Скачування ресурсів")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadResource(@Parameter(description = "Список шляхів до ресурсів які будуть скачені", example = "main/src/prod/")
                                 @RequestParam("path") String path, Authentication auth, HttpServletResponse response) throws IOException {

        User user = (User) auth.getPrincipal();
        service.download(path, user, response);
    }

    @Operation(summary = "Переміщення ресурсів та періймуваня")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не знайдений ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ресурс по пути to уже существует ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/move")
    public ResponseEntity<ResourceDTO> move(
            @Parameter(description = "Папку яку ми хочему переймувати", example = "main/src/prod/") @RequestParam("from") String from,
            @Parameter(description = "Як вона буде переймована", example = "main/src/prod2/") @RequestParam("to") String to, Authentication auth) {

        User user = (User) auth.getPrincipal();
        ResourceDTO resource = service.move(from, to, user);

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Знайти ресурс")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Невалідний,або відсутній шлях",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ResourceDTO>> find(
            @Parameter(description = "частина від імені ресурсу", example = "file.txt") @RequestParam("query") String query) {

        List<ResourceDTO> files = service.findResource(query);
        return ResponseEntity.ok(files);
    }
}

