package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.DTO.ResourceMoveDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ResourceNotExistException;
import com.example.cloudfilestorage.Mapper.ResourceMapper;
import com.example.cloudfilestorage.Repository.ResourceRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

import static io.micrometer.core.instrument.config.validate.DurationValidator.validate;


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

//    public void uploadFile(String bucket, String objectName, InputStream stream, long size, String contentType) {
//        try {
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucket)
//                            .object(objectName)
//                            .stream(stream, size, -1)
//                            .contentType(contentType)
//                            .build()
//            );
//
//        } catch (Exception e) {
//            throw new RuntimeException("Upload file was failed", e);
//        }
//    }


    public ResourceDTO getInfoAboutFile(String path, String ownerName) {
        Resource resource = getResource(path,ownerName);
        return resourceMapper.mapResourceDto(resource);
    }

    public void deleteResource(String path, String ownerName) {
        Resource resource = getResource(path,ownerName);
        resourceRepository.delete(resource);
    }

    public Resource getResource(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path;
        String fileName = path.substring(path.lastIndexOf("/") + 1);

        return resourceRepository
                .findResourceByOwnerIdAndPath(userId, fullPath, fileName)
                .orElseThrow(ResourceNotExistException::new);
    }

    public void deleteDirectory(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);

        resourceRepository.deleteDirectoryByOwnerIdAndPath(userId,path);
    }

    public FileDTO move(String from, String to, String username) {


    }
}
