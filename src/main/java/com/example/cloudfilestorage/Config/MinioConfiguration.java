package com.example.cloudfilestorage.Config;

import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("admin","password123") //todo properties
                .build();
    }

}
