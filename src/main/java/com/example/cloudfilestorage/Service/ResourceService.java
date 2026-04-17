package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.DirectoryDTO;
import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.ResourceType;
import com.example.cloudfilestorage.Entity.User;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ResourceNotExistException;
import com.example.cloudfilestorage.Mapper.ResourceMapper;
import com.example.cloudfilestorage.Repository.ResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


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


    public FileDTO getInfoAboutFile(String path, String ownerName) {
        Resource resource = getResource(path, ownerName);
        return resourceMapper.mapResourceDto(resource);
    }

    public void deleteResource(String path, String ownerName) {
        Resource resource = getResource(path, ownerName);
        resourceRepository.delete(resource);
    }

    public Resource getResource(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path;
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

    public List<FileDTO> findByQuery(String query) {
        List<Resource> resources = resourceRepository.findByNameContainingIgnoreCase(query);
        return resourceMapper.mapResourcesDto(resources);
    }

    public void uploadResource(String path, MultipartFile file, String username) {
        String fileName = file.getOriginalFilename();
        long size = file.getSize();
        Integer userId = userService.getUserIdByName(username);
        String fullPath = "user-" + userId + "-files/" + path;
        User user  = userService.getUserById(userId);

        Resource resource = new Resource(fullPath, fileName, size, ResourceType.FILE, user);

        resourceRepository.save(resource);
    }

    public List<FileDTO> getInfoAboutDirectory(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = "user-" + userId + "-files/" + path;
        List<Resource> resources = resourceRepository.findResourcesByOwnerIdAndPath(userId, fullPath);

        if (resources.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        return resourceMapper.mapResourcesDto(resources);
    }

    public DirectoryDTO createDirectory(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        User user = userService.getUserById(userId);
        String fullPath = "user-" + userId + "-files/" + path.substring(0,path.lastIndexOf("/") + 1);
        String directoryName = path.substring(path.lastIndexOf("/") + 1);

        Resource resource = new Resource(fullPath,directoryName,0L,ResourceType.DIRECTORY,user);
        Resource resource1 = resourceRepository.save(resource);

        return resourceMapper.mapDirectoryDTO(resource1);
    }
}
