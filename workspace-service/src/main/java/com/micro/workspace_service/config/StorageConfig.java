package com.micro.workspace_service.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@RequiredArgsConstructor
@Getter
@Setter
public class StorageConfig {
    // this is a class to generate beans and other utils to work with minio

    String url;
    String accessKey;
    String secretKey;
    @Bean
    public MinioClient generateClient(){
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey , secretKey)
                .build();
    }
}
