package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.ResourceDTO;
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

import static com.example.cloudfilestorage.Util.Util.BUCKET_NAME;

@Service
@Transactional
public class ResourceService {

    private final MinioClient minioClient;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final UserService userService;

    public ResourceService(MinioClient minioClient, ResourceRepository resourceRepository, ResourceMapper resourceMapper, UserService userService) {
        this.minioClient = minioClient;
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        createBucketIfNotExists(BUCKET_NAME);
    }

    private void createBucketIfNotExists(String nameOfBucket) {
        try {
            boolean isExist = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(nameOfBucket).build()
            );

            if (!isExist) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(nameOfBucket).build()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during creating bucket ", e);
        }

    }

    public void uploadFile(String bucket, String objectName, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Upload file was failed", e);
        }
    }


    public ResourceDTO getInfoAboutFile(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path;

        return resourceRepository
                .findResourceByOwnerIdAndPath(userId, fullPath)
                .map(resource -> resourceMapper.mapResourceDto(resource))
                .orElseThrow(() -> new ResourceNotExistException());
    }

    public void deleteResource(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path;

        Resource resource = resourceRepository.
                findResourceByOwnerIdAndPath(userId, fullPath)
                .orElseThrow(ResourceNotExistException::new);

        resourceRepository.delete(resource);
    }
}
