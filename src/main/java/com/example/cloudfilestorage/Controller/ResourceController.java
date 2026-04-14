package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Mapper.ResourceMapper;
import com.example.cloudfilestorage.Service.ResourceService;
import com.example.cloudfilestorage.Service.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.example.cloudfilestorage.Util.Util.BUCKET_NAME;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;
    private final StorageService storageService;

    public ResourceController(ResourceService resourceService, ResourceMapper resourceMapper, StorageService storageService) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Authentication auth) {
        String name = auth.getName();
        String filename = file.getOriginalFilename();
        String folders = "createLogic/db";

        String filePath = name + "/" + folders + "/" + filename;

        try {
            resourceService.uploadFile(
                    "file-storages",
                    filePath,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "redirect:/home";
    }

    @GetMapping
    public ResponseEntity getResource(@RequestParam String path, Authentication auth) {
        if (!path.isEmpty()) {
            String ownerName = auth.getName();
            ResourceDTO resourceDTO = resourceService.getInfoAboutFile(path, ownerName);

            FileDTO file = resourceMapper.mapFileDto(resourceDTO);

            return ResponseEntity.ok(file);
        } else {
            throw new InvalidResourcePathException();
        }
    }

    @DeleteMapping
    public ResponseEntity deleteResource(@RequestParam String path, Authentication auth) {
        if (!path.isEmpty()) {

            if (path.isEmpty()) {
                String ownerName = auth.getName();
                if (path.substring(path.length() - 1, path.length()).equals("/")) {
                    resourceService.deleteDirectory(path, ownerName);
                    storageService.deleteDirectory(path, ownerName);

                    return null;
                } else {
                    resourceService.deleteResource(path, ownerName);
                    storageService.deleteFile(path, ownerName);

                    return ResponseEntity.noContent().build();
                }
            } else {
                throw new InvalidResourcePathException();
            }

        } else {
            throw new InvalidResourcePathException();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam("path") String path) throws IOException {
        try {
            if (path.isEmpty()) {
                if (path.substring(path.length() - 1, path.length()).equals("/")) {
                    InputStream stream = storageService.getFile(BUCKET_NAME, path);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + path + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(new InputStreamResource(stream));
                } else {

                }
            } else {
                throw new InvalidResourcePathException();
            }

        } catch (Exception e) {
            throw new RuntimeException("File not found", e);
        }
    }
}
