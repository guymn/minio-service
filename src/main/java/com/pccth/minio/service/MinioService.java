package com.pccth.minio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.pccth.minio.dto.FileVersionInfo;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MinioService {

    private final MinioClient minioClient;
    private final AmazonS3 amazonS3Client;

    @Value("${minio.bucket-name}")
    private String BUCKET_NAME;

    // Generate presigned URL for uploading a specific part
    public String generatePresignedUrlDownload(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for part", e);
        }
    }

    public List<FileVersionInfo> listObjectVersions(String objectKey) {
        ListVersionsRequest request = new ListVersionsRequest()
                .withBucketName(BUCKET_NAME)
                .withPrefix(objectKey);

        VersionListing listing = amazonS3Client.listVersions(request);

        List<FileVersionInfo> versions = new ArrayList<>();
        for (S3VersionSummary versionSummary : listing.getVersionSummaries()) {
            versions.add(new FileVersionInfo(
                    versionSummary.getKey(),
                    versionSummary.getSize(),
                    versionSummary.getVersionId(),
                    versionSummary.isLatest(),
                    versionSummary.getLastModified()));
        }

        return versions;
    }

    public List<FileVersionInfo> listAllFiles() {
        List<FileVersionInfo> files = new ArrayList<>();
        ObjectListing listing = amazonS3Client.listObjects(BUCKET_NAME);

        // loop until all objects are listed
        while (true) {
            for (S3ObjectSummary summary : listing.getObjectSummaries()) {
                files.add(new FileVersionInfo(
                        summary.getKey(),
                        summary.getSize(), null, true,
                        summary.getLastModified()));
            }

            if (listing.isTruncated()) {
                listing = amazonS3Client.listNextBatchOfObjects(listing);
            } else {
                break;
            }
        }

        return files;
    }
}
