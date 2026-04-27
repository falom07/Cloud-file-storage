package com.example.cloudfilestorage.util;

import org.springframework.beans.factory.annotation.Value;

public class Util {
    @Value("${minio.bucket.name}")
    public static String BUCKET_NAME;
}
