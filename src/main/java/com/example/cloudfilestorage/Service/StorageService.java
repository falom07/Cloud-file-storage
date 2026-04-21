package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ResourceNotExistException;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public InputStream downloadFile(String pathOfFile) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(pathOfFile)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Getting file was failed", e);
        }
    }

    public void addFolderToZip(String path, ZipOutputStream zipOut) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(path)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                addFileToZip(item.objectName(), zipOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed during build zip file from folder ");
        }
    }

    public void addFileToZip(String path, ZipOutputStream zipOut) throws IOException {
        InputStream stream = downloadFile(path);
        String fileName = extractFileName(path);

        zipOut.putNextEntry(new ZipEntry(fileName));
        stream.transferTo(zipOut);
        zipOut.closeEntry();

        stream.close();
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public void deleteDirectory(String path, String username) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(path)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                deleteFile(item.objectName(), username);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed during delete folder ");
        }
    }

    public void move(String from, String to, String ownerName) {
        validate(from, to);

        if (!fileOrDirectoryIsExist(to)) {
            throw new ResourceNotExistException();
        }

        Integer userId = userService.getUserIdByName(ownerName);
        String fromFullPath = "user-" + userId + "-files/" + from;
        String toFullPath = "user-" + userId + "-files/" + to;

        if (from.endsWith("/")) {
            moveDirectory(fromFullPath, toFullPath, ownerName);
        } else {
            moveFiles(fromFullPath, toFullPath, ownerName);
        }
    }

    @SneakyThrows
    private void moveDirectory(String fromFullPath, String toFullPath, String ownerName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(fromFullPath)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            String from = result.get().objectName();
            String relativePath = from.substring(fromFullPath.length());
            String toPath = toFullPath + relativePath;

            moveFiles(from, toPath, ownerName);
        }
    }

    private void moveFiles(String from, String to, String ownerName) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(to)
                            .source(
                                    CopySource.builder()
                                            .bucket(BUCKET_NAME)
                                            .object(from)
                                            .build()
                            )
                            .build()
            );

            deleteFile(from, ownerName);

        } catch (Exception e) {
            throw new RuntimeException("Failed during moving or rename File");
        }
    }

    public boolean fileOrDirectoryIsExist(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(path)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validate(String from, String to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        if (from.equals(to)) {
            throw new InvalidResourcePathException();
        }
    }

    public void uploadResource(String path, List<MultipartFile> files, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = "user-" + userId + "-files/" + path;

        for(MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String objectName = fullPath + fileName;
            if (fileOrDirectoryIsExist(objectName)) { throw new InvalidResourcePathException(); }

            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed during upload file:" + fileName);
            }
        }
    }

    public void createDirectory(String path, String username) {
        Integer userId = userService.getUserIdByName(username);
        String fullPath = "user-" + userId + "-files/" + path.substring(0, path.lastIndexOf("/") + 1);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fullPath)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}),
                                    0,
                                    -1
                            )
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed during creating directory");
        }
    }
}
