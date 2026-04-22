package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.DirectoryDTO;
import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.ResourceType;
import com.example.cloudfilestorage.Entity.User;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ParentPathNotExistException;
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
        String fullPath = getFullPath(username, path.substring(0, path.lastIndexOf("/") + 1));
        String fileName = path.substring(path.lastIndexOf("/") + 1);

        return resourceRepository
                .findResourceByOwnerIdAndPathAndName(userId, fullPath, fileName)
                .orElseThrow(ResourceNotExistException::new);
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
        String directoryName = fullPath.substring(0, fullPath.length() - 1);
        directoryName = directoryName.substring(directoryName.lastIndexOf("/") + 1);
        return directoryName;
    }

    private static String getParentPath(String path, int levels) {
        for (int i = 0; i < levels; i++) {
            if (path.lastIndexOf("/") != path.indexOf("/")) {
                path = path.substring(0, path.length() - 1);
                path = path.substring(0, path.lastIndexOf("/") + 1);
            }
        }
        return path;
    }

    public FileDTO move(String from, String to, String username) {
        String fullPathFrom = getFullPath(username, from);
        String fullPathTo = getFullPath(username, to);

        if (isDirectory(from)) {
            moveDirectory(from, to, fullPathFrom, fullPathTo, username);
        } else {
            moveFile(from, to, fullPathFrom, fullPathTo);
        }

        return null;
    }

    private void moveFile(String from, String to, String fullPathFrom, String fullPathTo) {

        String fileNameFrom = fullPathFrom.substring(fullPathFrom.lastIndexOf("/") + 1);
        String fileNameTo = fullPathTo.substring(fullPathTo.lastIndexOf("/") + 1);
        String pathFrom = fullPathFrom.substring(0, fullPathFrom.lastIndexOf("/") + 1);
        String pathTo = fullPathTo.substring(0, fullPathTo.lastIndexOf("/") + 1);

        resourceRepository.updateFilePathAndName(pathFrom, pathTo, ResourceType.FILE, fileNameFrom, fileNameTo);
    }

    private void moveDirectory(String from, String to, String fullPathFrom, String fullPathTo, String username) {
        Integer userId = userService.getUserIdByName(username);

        String nameOfFileFrom = from.substring(0,from.length() - 1);
        nameOfFileFrom = nameOfFileFrom.substring(nameOfFileFrom.lastIndexOf("/") + 1);

        String nameOfFileTo = to.substring(0,to.length() - 1);
        nameOfFileTo = nameOfFileTo.substring(nameOfFileTo.lastIndexOf("/") + 1);

        String pathFrom = fullPathFrom.substring(0, fullPathFrom.lastIndexOf("/"));
        pathFrom = pathFrom.substring(0, pathFrom.lastIndexOf("/") + 1);

        String pathTo = fullPathTo.substring(0, fullPathTo.lastIndexOf("/"));
        pathTo = pathTo.substring(0, pathTo.lastIndexOf("/") + 1);

        try {
            resourceRepository.updateNameOfResource(nameOfFileFrom, nameOfFileTo, ResourceType.DIRECTORY, userId, pathFrom, pathTo);
            resourceRepository.updatePath(fullPathFrom, fullPathTo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
                    relavantPath = relavantPath.substring(0, relavantPath.length() - 1);
                    String directoryName = relavantPath.substring(relavantPath.lastIndexOf("/") + 1);
                    relavantPath = relavantPath.substring(0, relavantPath.lastIndexOf("/") + 1);
                    String finalPath = fullPath + relavantPath;
                    if (isResourceExist(finalPath, directoryName)) continue;
                    resources.add(saveDirectory(finalPath, directoryName, user));
                } else {
                    String fileName = relavantPath.substring(relavantPath.lastIndexOf("/") + 1);
                    relavantPath = relavantPath.substring(0, relavantPath.lastIndexOf("/") + 1);
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
        fullPath = fullPath.substring(0, fullPath.length() - 1);
        String directoryName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
        fullPath = fullPath.substring(0, fullPath.lastIndexOf("/") + 1);

        if (isResourceExist(fullPath, directoryName)) throw new ResourceAlreadyExistException();
//        if (!isParentPathExist(fullPath)) throw new ParentPathNotExistException();

        Resource resource = saveDirectory(fullPath, directoryName, user);

        return resourceMapper.mapDirectoryDTO(resource);
    }

    private @NonNull Resource saveDirectory(String fullPath, String directoryName, User user) {
        return resourceRepository.save(new Resource(
                fullPath, directoryName, 0L, ResourceType.DIRECTORY, user
        ));
    }

    private boolean isParentPathExist(String path) {
        Optional<Resource> resource = resourceRepository.getResourceByPath(path);
        return resource.isPresent();
    }

    private boolean isResourceExist(String fullPath, String resourceName) {
        Optional<Resource> resource = resourceRepository.getResourceByPathAndName(fullPath, resourceName);
        return resource.isPresent();
    }

    private String getFullPath(String username, String path) {
        Integer userId = userService.getUserIdByName(username);
        return "user-" + userId + "-files/" + path;
    }
}
