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
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
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
    private final RepositoryMethodInvocationListener repositoryMethodInvocationListener;
    private final StorageService storageService;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper, UserService userService, RepositoryMethodInvocationListener repositoryMethodInvocationListener, StorageService storageService) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
        this.userService = userService;
        this.repositoryMethodInvocationListener = repositoryMethodInvocationListener;
        this.storageService = storageService;
    }


    public ResourceDTO getInfoAboutFile(String path, String ownerName) {
        Resource resource = getResource(path, ownerName);
        return resourceMapper.mapFileDto(resource);
    }

    public void deleteResource(String path, String ownerName) {
        List<Resource> resources = getResources(path, ownerName);
        resourceRepository.deleteAll(resources);
    }

    public List<Resource> getResources(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path.substring(0, path.lastIndexOf("/") + 1);
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        //delete directory if it is last file

        if()
         resourceRepository
                .findResourceByOwnerIdAndPathAndName(userId, fullPath, fileName);

         return null;
    }

    public Resource getResource(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path.substring(0, path.lastIndexOf("/") + 1);
        String fileName = path.substring(path.lastIndexOf("/") + 1);

        return resourceRepository
                .findResourceByOwnerIdAndPathAndName(userId, fullPath, fileName)
                .orElseThrow(ResourceNotExistException::new);
    }

    public void deleteDirectory(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);

        resourceRepository.deleteDirectoryByOwnerIdAndPath(userId, path);
    }

    public FileDTO move(String from, String to, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPathFrom = "user-" + userId + "-files/" + from;
        String fullPathTo = "user-" + userId + "-files/" + to;
        Resource resource;


        String fileNameFrom = fullPathFrom.substring(fullPathFrom.lastIndexOf("/") + 1);
        String fileNameTo = fullPathTo.substring(fullPathTo.lastIndexOf("/") + 1);
        String path = fullPathFrom.substring(0, fullPathFrom.lastIndexOf("/") + 1);

        if (fileNameFrom.equals(fileNameTo)) { //move
            if (isDirectory(from)) {
                resourceRepository.updatePath(fullPathFrom, fullPathTo);
            } else {
                resourceRepository.updateNameOfResource(fileNameFrom, fileNameTo, "FILE", path);
            }
        } else { //rename
            if (isDirectory(from)) {
                resourceRepository.updateNameOfResource(fileNameFrom, fileNameTo, "DIRECTORY", path);
                resourceRepository.updatePath(fullPathFrom, fullPathTo);
            } else {
                resourceRepository.updateNameOfResource(fileNameFrom, fileNameTo, "FILE", path);
            }
        }

        return null;
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
        String fullPath = "user-" + userId + "-files/" + path;
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
                    if(isResourceExist(finalPath,directoryName)) continue;
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
        String fullPath = "user-" + userId + "-files/" + path;
        List<Resource> resources = resourceRepository.findResourcesByOwnerIdAndPath(userId, fullPath);

        return resourceMapper.mapDirectoriesDto(resources);
    }

    public DirectoryDTO createDirectories(String path, String username) {
        if (!path.endsWith("/") && !path.isEmpty()) throw new InvalidResourcePathException(); //400

        Integer userId = userService.getUserIdByName(username);
        User user = userService.getUserById(userId);
        String fullPath = "user-" + userId + "-files/" + path;
        fullPath = fullPath.substring(0, fullPath.length() - 1);
        String directoryName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
        fullPath = fullPath.substring(0, fullPath.lastIndexOf("/") + 1);

        if (isResourceExist(fullPath, directoryName)) throw new ResourceAlreadyExistException();
        if (!isParentPathExist(fullPath)) throw new ParentPathNotExistException();

        Resource resource = saveDirectory(fullPath, directoryName, user);

        return resourceMapper.mapDirectoryDTO(resource, "");
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

}
