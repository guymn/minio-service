package com.pccth.minio.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.pccth.minio.dto.CompleteUploadRequest;
import com.pccth.minio.dto.MultipartUploadResponse;
import com.pccth.minio.dto.PartInfo;
import com.pccth.minio.dto.PartPresignedUrl;

import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MinioMultipartService {
    private final MinioClient minioClient;
    private final AmazonS3 amazonS3Client;

    @Value("${minio.bucket-name}")
    private String BUCKET_NAME;

    // Initiate multipart upload and return upload ID
    public MultipartUploadResponse initiateMultipartUpload(String objectName,
            String contentType) {
        try {
            // Generate a unique upload ID
            InitiateMultipartUploadResult result = amazonS3Client
                    .initiateMultipartUpload(new InitiateMultipartUploadRequest(
                            BUCKET_NAME,
                            objectName));

            return new MultipartUploadResponse(result.getUploadId(), objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate multipart upload", e);
        }
    }

    // Generate presigned URL for uploading a specific part
    public String generatePresignedUrlForPart(String objectName,
            String uploadId, int partNumber) {
        try {
            // Create a unique object name for this part
            String partObjectName = generatePartObjectNameForTemp(objectName, uploadId, partNumber);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(BUCKET_NAME)
                            .object(partObjectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for part", e);
        }
    }

    // Generate multiple presigned URLs for parts
    public List<PartPresignedUrl> generatePresignedUrlsForParts(String objectName,
            String uploadId, int totalParts) {
        List<PartPresignedUrl> urls = new ArrayList<>();

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            String presignedUrl = generatePresignedUrlForPart(objectName, uploadId, partNumber);
            urls.add(new PartPresignedUrl(partNumber, presignedUrl));
        }

        return urls;
    }

    // Complete multipart upload by composing all parts
    public ObjectWriteResponse completeMultipartUpload(CompleteUploadRequest request) {
        try {
            // Sort parts by part number
            request.getParts().sort(Comparator.comparing(PartInfo::getPartNumber));

            // Create compose sources from uploaded parts
            List<ComposeSource> sources = new ArrayList<>();
            for (PartInfo part : request.getParts()) {
                String partObjectName = generatePartObjectNameForTemp(request.getObjectName(), request.getUploadId(),
                        part.getPartNumber());
                sources.add(
                        ComposeSource.builder()
                                .bucket(BUCKET_NAME)
                                .object(partObjectName)
                                .build());
            }

            ObjectWriteResponse response = minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(request.getObjectName())
                            .sources(sources)
                            .build());

            // Clean up part objects and all version
            cleanupPartObjectsAllVersion(request.getObjectName(), request.getUploadId(), request.getParts());

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to complete multipart upload", e);
        }
    }

    // Abort multipart upload and cleanup
    public void abortMultipartUpload(String objectName, String uploadId) {
        try {
            cleanupAllPartObjectsAllVersion(objectName, uploadId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to abort multipart upload", e);
        }
    }

    private String generatePartObjectNameForTemp(String objectName, String uploadId, int partNumber) {
        return String.format(".temp/.multipart-%s/%s/part-%05d", uploadId, objectName, partNumber);
    }

    // การลบ object part (removeObject) จะไม่ลบจริง แต่จะใส่ Delete Marker Storage
    // ของคุณยังเก็บทุก part (version เก่า) → เปลืองเนื้อที่
    private void cleanupPartObjects(String objectName, String uploadId, List<PartInfo> parts) {
        for (PartInfo part : parts) {
            try {
                String partObjectName = generatePartObjectNameForTemp(objectName, uploadId, part.getPartNumber());
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(partObjectName)
                                .build());
            } catch (Exception e) {
                // Log error but don't fail the operation
                System.err.println("Failed to cleanup part object: " + e.getMessage());
            }
        }
    }

    // การลบ object part (removeObject) จะไม่ลบจริง แต่จะใส่ Delete Marker Storage
    // ของคุณยังเก็บทุก part (version เก่า) → เปลืองเนื้อที่
    private void cleanupAllPartObjects(String objectName, String uploadId) {
        try {
            String prefix = String.format(".multipart-%s/%s/", uploadId, objectName);

            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(prefix)
                            .build());

            for (Result<Item> result : objects) {
                Item item = result.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(item.objectName())
                                .build());
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup part objects: " + e.getMessage());
        }
    }

    private void cleanupAllPartObjectsAllVersion(String objectName, String uploadId) {
        try {
            String prefix = String.format(".multipart-%s/%s/", uploadId, objectName);

            // 1. ดึง version ทั้งหมดของ prefix นั้น
            ListVersionsRequest listVersionsRequest = new ListVersionsRequest()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix(prefix);

            VersionListing versionListing = amazonS3Client.listVersions(listVersionsRequest);

            for (S3VersionSummary version : versionListing.getVersionSummaries()) {
                amazonS3Client.deleteVersion(
                        version.getBucketName(),
                        version.getKey(),
                        version.getVersionId());
            }

        } catch (Exception e) {
            System.err.println("Failed to cleanup part object versions: " + e.getMessage());
        }
    }

    private void cleanupPartObjectsAllVersion(String objectName, String uploadId, List<PartInfo> parts) {
        for (PartInfo part : parts) {
            try {
                String partObjectName = generatePartObjectNameForTemp(objectName, uploadId, part.getPartNumber());
                // ดึง version ทั้งหมดของ part object
                ListVersionsRequest request = new ListVersionsRequest()
                        .withBucketName(BUCKET_NAME)
                        .withPrefix(partObjectName);

                VersionListing listing = amazonS3Client.listVersions(request);
                for (S3VersionSummary version : listing.getVersionSummaries()) {
                    if (version.getKey().equals(partObjectName)) {
                        amazonS3Client.deleteVersion(BUCKET_NAME, partObjectName, version.getVersionId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to cleanup part object: " + e.getMessage());
            }
        }
    }

}
