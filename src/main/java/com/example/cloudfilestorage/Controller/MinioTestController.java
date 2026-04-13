package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.Config.MinioConfiguration;
import com.example.cloudfilestorage.Service.MinioService;
import io.minio.MinioClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/files")
public class MinioTestController {

    private final MinioService minioService;

    public MinioTestController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Authentication auth) {
        String name = auth.getName();
        String filename =  file.getOriginalFilename();
        String folders = "createLogic/db";

        String filePath = name + "/" + folders + "/" + filename;

        try {
            minioService.uploadFile(
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

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("file") String file) throws IOException {
        try {
            InputStream stream = minioService.getFile("images", file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            throw new RuntimeException("File not found", e);
        }

    }
}
