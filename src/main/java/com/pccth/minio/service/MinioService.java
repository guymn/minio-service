package com.pccth.minio.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pccth.minio.entity.TestFileEntity;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

        private final MinioClient minioClient;
        private final BucketService bucketService;
        private final TestFileService testFileService;

        // ✅ อัปโหลดไฟล์
        public void uploadFile(String bucketName, String objectName, MultipartFile file) throws Exception {
                try {
                        bucketService.createBucketIfNotExists(bucketName);

                        TestFileEntity fileEntity = new TestFileEntity(null, objectName, file.getSize(), bucketName);
                        testFileService.save(fileEntity);
                        minioClient.putObject(
                                        PutObjectArgs.builder()
                                                        .bucket(bucketName)
                                                        .object(objectName)
                                                        .stream(file.getInputStream(), file.getSize(), -1)
                                                        .contentType(file.getContentType())
                                                        .build());
                } catch (Exception e) {
                        throw new IllegalAccessException(e.getMessage());
                }

        }

        // ✅ ดาวน์โหลดไฟล์
        public InputStream downloadFile(String bucketName, String objectName) throws Exception {
                return minioClient.getObject(
                                GetObjectArgs.builder()
                                                .bucket(bucketName)
                                                .object(objectName)
                                                .build());
        }

        // ✅ สร้าง Presigned URL สำหรับดาวน์โหลด (เฉพาะ private bucket)
        public String generatePresignedUrl(String bucketName, String objectName, int expireSeconds) throws Exception {
                return minioClient.getPresignedObjectUrl(
                                GetPresignedObjectUrlArgs.builder()
                                                .method(Method.GET)
                                                .bucket(bucketName)
                                                .object(objectName)
                                                .expiry(expireSeconds, TimeUnit.SECONDS)
                                                .build());
        }

        // ✅ ลบไฟล์
        public void deleteFile(String bucketName, String objectName) throws Exception {
                minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                                .bucket(bucketName)
                                                .object(objectName)
                                                .build());
        }

}
