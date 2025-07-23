package com.pccth.minio.service;

import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BucketService {
    private final MinioClient minioClient;

    // ✅ สร้าง Bucket (ถ้ายังไม่มี)
    public void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    // ✅ ตั้ง bucket เป็น public
    public void setBucketPublic(String bucketName) throws Exception {
        String policyJson = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policyJson)
                        .build());
    }

    // ✅ ตั้ง bucket เป็น private (ลบ policy)
    public void setBucketPrivate(String bucketName) throws Exception {
        minioClient.deleteBucketPolicy(
                DeleteBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .build());
    }

    public String checkPolicy(String bucketName) throws Exception {
        String currentPolicy = minioClient.getBucketPolicy(
                GetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .build());
        System.out.println("Current policy:\n" + currentPolicy);
        return currentPolicy;
    }
}
