package com.example.cloudfilestorage.Service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Transactional
public class MinioService {

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        createBucketIfNotExists("file-storages");
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
