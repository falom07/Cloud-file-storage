package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.DTO.ResourceMoveDTO;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Mapper.ResourceMapper;
import com.example.cloudfilestorage.Service.ResourceService;
import com.example.cloudfilestorage.Service.StorageService;
import com.example.cloudfilestorage.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;
    private final StorageService storageService;
    private final UserService userService;

    public ResourceController(ResourceService resourceService, ResourceMapper resourceMapper, StorageService storageService, UserService userService) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
        this.storageService = storageService;
        this.userService = userService;
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
    public ResponseEntity deleteResource(@RequestParam List<String> path, Authentication auth) {

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





    @GetMapping("/download")
    public void downloadResource(
            @RequestParam("path") List<String> path, Authentication auth, HttpServletResponse response) throws IOException {

        if ( path == null || path.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        String username = auth.getName();

        if (path.size() == 1 && !path.get(0).endsWith("/")) {
            downloadSingleFile(path.get(0), username, response);
        } else {
            downloadZip(path, username, response);
        }
    }

    @GetMapping("/move")
    public ResponseEntity<FileDTO> move(
            @RequestParam("from") String from ,@RequestParam("to") String to,Authentication auth){
        String username = auth.getName();

        storageService.move(from,to,username);
        FileDTO resource = resourceService.move(from,to,username);

        return ResponseEntity.ok(resource);
    }


//todo Move to Service
    private void downloadSingleFile(String path, String username, HttpServletResponse response) throws IOException {
        ResourceDTO dto = resourceService.getInfoAboutFile(path, username);
        String fullPath = dto.path() + dto.name();
        InputStream stream = storageService.downloadFile(fullPath);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + extractFileName(path) + "\"");

        stream.transferTo(response.getOutputStream());
        stream.close();
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void downloadZip(List<String> paths, String username, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=download.zip");

        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        Integer userId = userService.getUserIdByName(username);

        for (String path : paths) {
            String fullPath = "user-" + userId + "-files/" + path;

            if (path.endsWith("/")) {
                storageService.addFolderToZip(fullPath, zipOut);
            } else {
                storageService.addFileToZip(fullPath, zipOut);
            }
        }
        zipOut.finish();
    }

    private void deleteSingleFile(String path, String ownerName) {
        resourceService.deleteResource(path, ownerName);
        storageService.deleteFile(path, ownerName);
    }

    private void deleteDirectory(String path, String username) {
        storageService.deleteDirectory(path,username);
        resourceService.deleteDirectory(path,username);
    }

    private void deleteFilesOrDirectory(List<String> paths, String username) {
        for (String path : paths) {
            if (path.endsWith("/")) {
                deleteDirectory(path,username);
            } else {
                deleteSingleFile(path, username);
            }
        }
    }
}

