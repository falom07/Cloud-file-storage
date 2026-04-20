package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.DirectoryDTO;
import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.Service.ResourceService;
import com.example.cloudfilestorage.Service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final ResourceService resourceService;
    private final StorageService storageService;

    public DirectoryController(ResourceService resourceService, StorageService storageService) {
        this.resourceService = resourceService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<FileDTO>> getInfo(@RequestParam("path") String path, Authentication auth){
        String username = auth.getName();
        List<FileDTO> list = resourceService.getInfoAboutDirectory(path,username);

        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<DirectoryDTO> createDirectory(@RequestParam("path") String path, Authentication auth){
        String username = auth.getName();
        DirectoryDTO directory = resourceService.createDirectory(path,username);
        storageService.createDirectory(path,username);

        return ResponseEntity.ok(directory);
    }

}
