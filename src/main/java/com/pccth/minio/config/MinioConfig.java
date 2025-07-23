package com.pccth.minio.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000") // เปลี่ยนถ้าใช้ port อื่นหรือ IP server
                .credentials("minioadmin", "minioadmin123") // เปลี่ยนถ้าคุณตั้งค่าใหม่
                .build();
    }
}
