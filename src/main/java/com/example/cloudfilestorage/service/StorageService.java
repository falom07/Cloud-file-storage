package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.exception.InvalidResourcePathException;
import com.example.cloudfilestorage.exception.ResourceAlreadyExistException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.example.cloudfilestorage.util.Util.BUCKET_NAME;

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

    public void deleteFile(String path, String username) {
        String fullPath = getFullPath(path, username);

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

    public InputStream downloadFile(String fullPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fullPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Getting file was failed", e);
        }
    }

    public void addFolderToZip(String username, ZipOutputStream zipOut, String path) {
        String fullPath = getFullPath(path, username);

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(fullPath)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                addFileToZip(username, zipOut, result.get().objectName(), fullPath);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed during build zip file from folder ");
        }
    }

    private String getFullPath(String path, String username) {
        long userId = userService.getUserIdByName(username);
        return "user-" + userId + "-files/" + path;
    }

    public void addFileToZip(String username, ZipOutputStream zipOut, String fullPath, String relativePath) throws IOException {
        InputStream stream = downloadFile(fullPath);
        String fileName = fullPath.substring(relativePath.length());

        zipOut.putNextEntry(new ZipEntry(fileName));
        stream.transferTo(zipOut);
        zipOut.closeEntry();

        stream.close();
    }

    private String extractFileName(String fullPath, String relativePath) {
        String path = relativePath.replace(fullPath,"");
        return path.substring(path.length() - 1);
    }

    public void deleteDirectory(String path, String username) {
        String fullPath = getFullPath(path, username);
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(fullPath)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                String objectName = getObjectName(result);
                deleteFile(objectName, username);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed during delete folder ");
        }
    }

    private static String getObjectName(Result<Item> result) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        String objectName = result.get().objectName();
        objectName = objectName.substring(objectName.indexOf("/") + 1);
        return objectName;
    }

    public void move(String from, String to, String username) {
        String fromFullPath = getFullPath(from, username);
        String toFullPath = getFullPath(to, username);

        validatePaths(fromFullPath, toFullPath);


        if (from.endsWith("/")) {
            moveDirectory(fromFullPath, toFullPath, username);
        } else {
            moveFiles(fromFullPath, toFullPath, username);
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

    private void moveFiles(String from, String to, String username) {

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

            String relativePath = from.substring(from.indexOf("/") + 1);
            deleteFile(relativePath, username);

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

    private void validatePaths(String from, String to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            throw new InvalidResourcePathException();
        }

        if (from.equals(to)) {
            throw new InvalidResourcePathException();
        }

        if (fileOrDirectoryIsExist(to)) {
            throw new ResourceAlreadyExistException();
        }
    }

    public void uploadResource(String path, List<MultipartFile> files, String username) {
        String fullPath = getFullPath(path, username);

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String objectName = fullPath + fileName;
            if (fileOrDirectoryIsExist(objectName)) {
                throw new InvalidResourcePathException();
            }

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
        String fullPath = getFullPath(path.substring(0, path.lastIndexOf("/") + 1), username);

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

    public InputStream downloadFile(String username, ResourceDTO dto) {
        String relevantPath = dto.path() + dto.name();
        String fullPath = getFullPath(relevantPath,username);
        return downloadFile(fullPath);
    }
}
