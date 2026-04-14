package com.example.cloudfilestorage.Service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.example.cloudfilestorage.Util.Util.BUCKET_NAME;

@Service
@Transactional
public class StorageService {

    private final MinioClient minioClient;
    private final UserService userService;

    public StorageService(MinioClient minioClient, UserService userService) {
        this.minioClient = minioClient;
        this.userService = userService;
    }

    public void deleteFile(String path, String ownerName) {
        Integer userId = userService.getUserIdByName(ownerName);
        String fullPath = "user-" + userId + "-files/" + path;

        try {
            minioClient.
                    removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(BUCKET_NAME)
                                    .object(fullPath)
                                    .build()
                    );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    public InputStream getFile(String bucket, String nameOfFile) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(nameOfFile)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Geting file was failed", e);
        }
    }
}
