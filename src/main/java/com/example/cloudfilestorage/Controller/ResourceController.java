package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.ErrorResponse;
import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Service.ResourceService;
import com.example.cloudfilestorage.Service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/resource")
@Tag(name = "Resource", description = "Керуванням файлами та дерикторіями")
public class ResourceController {

    private final ResourceService resourceService;
    private final StorageService storageService;

    public ResourceController(ResourceService resourceService, StorageService storageService) {
        this.resourceService = resourceService;
        this.storageService = storageService;
    }

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
        if (!path.isEmpty()) {
            String ownerName = auth.getName();
            ResourceDTO fileDto = resourceService.getInfoAboutFile(path, ownerName);

            return ResponseEntity.ok(fileDto);
        } else {
            throw new InvalidResourcePathException();
        }
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

        if (path == null || path.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        String username = auth.getName();

        if (path.size() == 1 && !path.get(0).endsWith("/")) {
            deleteSingleFile(path.get(0), username);
        } else {
            deleteFilesOrDirectory(path, username);
        }

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
        String username = auth.getName();
        System.out.println("upload resources ");

        storageService.uploadResource(path, files, username);
        List<ResourceDTO> resourceDTO = resourceService.uploadResource(path, files, username);

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
    @GetMapping("/download")
    public void downloadResource(@Parameter(description = "Список шляхів до ресурсів які будуть скачені", example = "main/src/prod/")
                                 @RequestParam("path") String path, Authentication auth, HttpServletResponse response) throws IOException {

        if (path == null || path.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        String username = auth.getName();

        if (!path.endsWith("/")) {
            downloadSingleFile(path, username, response);
        } else {
            downloadZip(path, username, response);
        }
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
    public ResponseEntity<FileDTO> move(
            @Parameter(description = "Папку яку ми хочему переймувати", example = "main/src/prod/") @RequestParam("from") String from,
            @Parameter(description = "Як вона буде переймована", example = "main/src/prod2/") @RequestParam("to") String to, Authentication auth) {
        String username = auth.getName();

        storageService.move(from, to, username);
        FileDTO resource = resourceService.move(from, to, username);

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
        List<ResourceDTO> files = resourceService.findByQuery(query);
        return ResponseEntity.ok(files);
    }


    private void downloadSingleFile(String path, String username, HttpServletResponse response) throws IOException {
        FileDTO dto = (FileDTO) resourceService.getInfoAboutFile(path, username);
        InputStream stream = storageService.downloadFile(username, dto);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + extractFileName(path) + "\"");

        stream.transferTo(response.getOutputStream());
        stream.close();
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void downloadZip(String path, String username, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=download.zip");

        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        storageService.addFolderToZip(username, zipOut, path);

        zipOut.finish();
    }

    private void deleteSingleFile(String path, String ownerName) {
        resourceService.deleteResource(path, ownerName);
        storageService.deleteFile(path, ownerName);
    }

    private void deleteDirectory(String path, String username) {
        storageService.deleteDirectory(path, username);
        resourceService.deleteDirectory(path, username);
    }

    private void deleteFilesOrDirectory(List<String> paths, String username) {
        for (String path : paths) {
            if (path.endsWith("/")) {
                deleteDirectory(path, username);
            } else {
                deleteSingleFile(path, username);
            }
        }
    }
}

