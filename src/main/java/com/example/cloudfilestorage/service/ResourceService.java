package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.Repository.ResourceRepository;
import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.Resource;
import com.example.cloudfilestorage.entity.ResourceType;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.exception.InvalidResourcePathException;
import com.example.cloudfilestorage.exception.ResourceAlreadyExistException;
import com.example.cloudfilestorage.exception.ResourceNotExistException;
import com.example.cloudfilestorage.mapper.ResourceMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserService userService;

    public ResourceDTO getInfoAboutFile(String path, Integer userId) {
        Resource resource = getResource(path, userId);
        return resourceMapper.mapFileDto(resource);
    }

    public void deleteResource(String path, Integer userId) {
        Resource resources = getResource(path, userId);
        resourceRepository.delete(resources);
    }

    public Resource getResource(String path, Integer userId) {
        String fullPath = getFullPath(userId, getParentPath(path));
        String fileName = getResourceName(path);

        return resourceRepository
                .findResourceByOwnerIdAndPathAndName(userId, fullPath, fileName)
                .orElseThrow(() -> new ResourceNotExistException("Resource does not exist "));
    }

    public ResourceDTO move(String from, String to, Integer userId) {
        String fullPathFrom = getFullPath(userId, from);
        String fullPathTo = getFullPath(userId, to);

        if (isDirectory(from)) {
            return moveDirectory(from, to, fullPathFrom, fullPathTo, userId);
        } else {
            return moveFile(fullPathFrom, fullPathTo, userId);
        }
    }



    public List<ResourceDTO> findByQuery(String query) {
        List<Resource> resources = resourceRepository.findByNameContainingIgnoreCase(query);
        return resourceMapper.mapResourcesDto(resources);
    }

    public List<ResourceDTO> uploadResource(String path, List<MultipartFile> files, Integer userId) {
        if (!path.endsWith("/") && !path.isEmpty()) throw new InvalidResourcePathException("Invalid path");

        String fullPath = getFullPath(userId, path);
        User user = userService.getUserById(userId);
        List<Resource> resources = new ArrayList<>();

        for (MultipartFile file : files) {
            String relavantPath = file.getOriginalFilename();
            long size = file.getSize();
            do {
                if (relavantPath.endsWith("/")) {
                    relavantPath = getPathWithOutSlash(relavantPath);
                    String directoryName = getResourceName(relavantPath);
                    relavantPath = getParentPath(relavantPath);
                    String finalPath = fullPath + relavantPath;
                    if (isResourceExist(finalPath, directoryName)) continue;
                    resources.add(saveDirectory(finalPath, directoryName, user));
                } else {
                    String fileName = getResourceName(relavantPath);
                    relavantPath = getParentPath(relavantPath);
                    String finalPath = fullPath + relavantPath;
                    resources.add(saveFile(finalPath, fileName, size, user));
                }
            } while (relavantPath.contains("/"));
        }

        return resourceMapper.mapResourcesDto(resources);
    }

    public void deleteDirectory(String path, Integer userId) {
        String fullPath = getFullPath(userId, path);
        resourceRepository.deleteResourcesByOwnerIdAndPath(userId, fullPath);

        String directoryName = getDirectoryName(fullPath);
        fullPath = getParentPath(fullPath, 1);
        resourceRepository.deleteDirectoryByOwnerIdAndPathAndName(userId, fullPath, directoryName);
    }

    public List<ResourceDTO> getInfoAboutDirectory(String path, Integer userId) {
        String fullPath = getFullPath(userId, path);
        List<Resource> resources = resourceRepository.findResourcesByOwnerIdAndPath(userId, fullPath);

        return resourceMapper.mapResourcesDto(resources);
    }

    public ResourceDTO createDirectories(String path, Integer userId) {
        if (!path.endsWith("/") && !path.isEmpty()) throw new InvalidResourcePathException("invalid path"); //400

        User user = userService.getUserById(userId);
        String fullPath = getFullPath(userId, path);
        fullPath = getPathWithOutSlash(fullPath);
        String directoryName = getResourceName(fullPath);
        fullPath = getParentPath(fullPath);

        if (isResourceExist(fullPath, directoryName)) throw new ResourceAlreadyExistException("Resource alredy exist");

        Resource resource = saveDirectory(fullPath, directoryName, user);

        return resourceMapper.mapDirectoryDTO(resource);
    }

    private Resource saveFile(String fullPath, String fileName, long size, User user) {
        Resource resource = new Resource(fullPath, fileName, size, ResourceType.FILE, user);
        return resourceRepository.save(resource);
    }

    private static String getResourceName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static String getPathWithOutSlash(String path) {
        return path.substring(0, path.length() - 1);
    }

    private static String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    private Resource saveDirectory(String fullPath, String directoryName, User user) {
        return resourceRepository.save(new Resource(
                fullPath, directoryName, 0L, ResourceType.DIRECTORY, user
        ));
    }

    private boolean isResourceExist(String fullPath, String resourceName) {
        Optional<Resource> resource = resourceRepository.getResourceByPathAndName(fullPath, resourceName);
        return resource.isPresent();
    }

    private String getFullPath(Integer userId, String path) {
        return "user-" + userId + "-files/" + path;
    }

    private String getDirectoryName(String fullPath) {
        String directoryName = getPathWithOutSlash(fullPath);
        directoryName = getResourceName(directoryName);
        return directoryName;
    }

    private static String getParentPath(String path, int levels) {
        for (int i = 0; i < levels; i++) {
            if (path.lastIndexOf("/") != path.indexOf("/")) {
                path = getPathWithOutSlash(path);
                path = getParentPath(path);
            }
        }
        return path;
    }

    private ResourceDTO moveFile(String fullPathFrom, String fullPathTo, Integer userId) {

        String fileNameFrom = getResourceName(fullPathFrom);
        String fileNameTo = getResourceName(fullPathTo);
        String pathFrom = getParentPath(fullPathFrom);
        String pathTo = getParentPath(fullPathTo);

        resourceRepository.updateFilePathAndName(pathFrom, pathTo, ResourceType.FILE, fileNameFrom, fileNameTo);
        Resource resource = resourceRepository.getResourceByPathAndNameAndUserIdAndType(pathTo, fileNameTo, userId, ResourceType.FILE);
        return resourceMapper.mapFileDto(resource);
    }

    private ResourceDTO moveDirectory(String from, String to, String fullPathFrom, String fullPathTo, Integer userId) {

        String nameOfFileFrom = getPathWithOutSlash(from);
        nameOfFileFrom = getResourceName(nameOfFileFrom);

        String nameOfFileTo = getPathWithOutSlash(to);
        nameOfFileTo = getResourceName(nameOfFileTo);

        String pathFrom = fullPathFrom.substring(0, fullPathFrom.lastIndexOf("/"));
        pathFrom = getParentPath(pathFrom);

        String pathTo = fullPathTo.substring(0, fullPathTo.lastIndexOf("/"));
        pathTo = getParentPath(pathTo);

        resourceRepository.updateNameAndPathOfResource(nameOfFileFrom, nameOfFileTo, ResourceType.DIRECTORY, userId, pathFrom, pathTo);
        resourceRepository.updatePaths(fullPathFrom, fullPathTo);

        Resource resource = resourceRepository.getResourceByPathAndNameAndUserIdAndType(pathTo, nameOfFileTo, userId, ResourceType.DIRECTORY);
        return resourceMapper.mapDirectoryDTO(resource);
    }

    private boolean isDirectory(String from) {
        return from.charAt(from.length() - 1) == '/';
    }
}
