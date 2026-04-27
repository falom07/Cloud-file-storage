package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.exception.InvalidResourcePathException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FacadeService {
    private final StorageService storageService;
    private final ResourceService resourceService;

    public ResourceDTO move(String from, String to, User user) {
        storageService.move(from, to, user.getId());
        return resourceService.move(from, to, user.getId());
    }

    public void download(String path, User user, HttpServletResponse response) throws IOException {
        if (path == null || path.isEmpty()) {
            throw new InvalidResourcePathException("Invalid path");
        }

        if (!path.endsWith("/")) {
            downloadSingleFile(path, user.getId(), response);
        } else {
            downloadZip(path, user.getId(), response);
        }
    }

    public ResourceDTO get(String path, User user) {
        if (!path.isEmpty()) {
            return resourceService.getInfoAboutFile(path, user.getId());
        } else {
            throw new InvalidResourcePathException("Invalid path");
        }
    }

    public void delete(List<String> path, User user) {
        if (path == null || path.isEmpty()) {
            throw new InvalidResourcePathException("Invalid path");
        }

        if (path.size() == 1 && !path.get(0).endsWith("/")) {
            deleteSingleFile(path.get(0), user.getId());
        } else {
            deleteFilesOrDirectory(path, user.getId());
        }
    }

    public List<ResourceDTO> findResource(String query) {
        return resourceService.findByQuery(query);
    }

    public List<ResourceDTO> upload(String path, List<MultipartFile> files, User user) {
        storageService.uploadResource(path, files, user.getId());
        return resourceService.uploadResource(path, files, user.getId());
    }

    private void downloadSingleFile(String path, Integer userId, HttpServletResponse response) throws IOException {
        ResourceDTO dto = resourceService.getInfoAboutFile(path, userId);
        InputStream stream = storageService.downloadFile(userId, dto);

        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + extractFileName(path) + "\"");

        stream.transferTo(response.getOutputStream());
        stream.close();
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void downloadZip(String path, Integer userId, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=download.zip");

        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        storageService.addFolderToZip(userId, zipOut, path);

        zipOut.finish();
    }

    private void deleteFilesOrDirectory(List<String> paths, Integer userId) {
        for (String path : paths) {
            if (path.endsWith("/")) {
                deleteDirectory(path, userId);
            } else {
                deleteSingleFile(path, userId);
            }
        }
    }

    private void deleteSingleFile(String path, Integer userId) {
        resourceService.deleteResource(path, userId);
        storageService.deleteFile(path, userId);
    }

    private void deleteDirectory(String path, Integer userId) {
        storageService.deleteDirectory(path, userId);
        resourceService.deleteDirectory(path, userId);
    }
}
