package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.DirectoryDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.ResourceType;
import com.example.cloudfilestorage.Entity.User;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ResourceAlreadyExistException;
import com.example.cloudfilestorage.Exception.ResourceNotExistException;
import com.example.cloudfilestorage.Mapper.ResourceMapper;
import com.example.cloudfilestorage.Repository.ResourceRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserService userService;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper, UserService userService) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
        this.userService = userService;
    }


    public ResourceDTO getInfoAboutFile(String path, String username) {
        Resource resource = getResource(path, username);
        return resourceMapper.mapFileDto(resource);
    }

    public void deleteResource(String path, String ownerName) {
        Resource resources = getResource(path, ownerName);
        resourceRepository.delete(resources);
    }

    public Resource getResource(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = getFullPath(username, getParentPath(path));
        String fileName = getResourceName(path);

        return resourceRepository
                .findResourceByOwnerIdAndPathAndName(userId, fullPath, fileName)
                .orElseThrow(ResourceNotExistException::new);
    }



    public ResourceDTO move(String from, String to, String username) {
        String fullPathFrom = getFullPath(username, from);
        String fullPathTo = getFullPath(username, to);
        Integer userId = userService.getUserIdByName(username);

        if (isDirectory(from)) {
            return moveDirectory(from, to, fullPathFrom, fullPathTo, userId);
        } else {
            return moveFile(fullPathFrom, fullPathTo, userId);
        }
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

    public List<ResourceDTO> findByQuery(String query) {
        List<Resource> resources = resourceRepository.findByNameContainingIgnoreCase(query);
        return resourceMapper.mapResourcesDto(resources);
    }

    public List<ResourceDTO> uploadResource(String path, List<MultipartFile> files, String username) {
        if (!path.endsWith("/") && !path.isEmpty()) throw new InvalidResourcePathException();

        Integer userId = userService.getUserIdByName(username);
        String fullPath = getFullPath(username, path);
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

    private Resource saveFile(String fullPath, String fileName, long size, User user) {
        Resource resource = new Resource(fullPath, fileName, size, ResourceType.FILE, user);
        return resourceRepository.save(resource);
    }

    public List<ResourceDTO> getInfoAboutDirectory(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = getFullPath(username, path);
        List<Resource> resources = resourceRepository.findResourcesByOwnerIdAndPath(userId, fullPath);

        return resourceMapper.mapDirectoriesDto(resources);
    }

    public DirectoryDTO createDirectories(String path, String username) {
        if (!path.endsWith("/") && !path.isEmpty()) throw new InvalidResourcePathException(); //400

        Integer userId = userService.getUserIdByName(username);
        User user = userService.getUserById(userId);
        String fullPath = getFullPath(username, path);
        fullPath = getPathWithOutSlash(fullPath);
        String directoryName = getResourceName(fullPath);
        fullPath = getParentPath(fullPath);

        if (isResourceExist(fullPath, directoryName)) throw new ResourceAlreadyExistException();

        Resource resource = saveDirectory(fullPath, directoryName, user);

        return resourceMapper.mapDirectoryDTO(resource);
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

    private String getFullPath(String username, String path) {
        Integer userId = userService.getUserIdByName(username);
        return "user-" + userId + "-files/" + path;
    }

    public void deleteDirectory(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = getFullPath(username, path);
        resourceRepository.deleteResourcesByOwnerIdAndPath(userId, fullPath);

        String directoryName = getDirectoryName(fullPath);
        fullPath = getParentPath(fullPath, 1);
        resourceRepository.deleteDirectoryByOwnerIdAndPathAndName(userId, fullPath, directoryName);
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
}
