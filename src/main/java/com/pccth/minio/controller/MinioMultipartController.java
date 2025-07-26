package com.pccth.minio.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pccth.minio.dto.CompleteUploadRequest;
import com.pccth.minio.dto.MultipartUploadInfo;
import com.pccth.minio.dto.MultipartUploadResponse;
import com.pccth.minio.dto.PartPresignedUrl;
import com.pccth.minio.dto.Response;
import com.pccth.minio.service.MinioMultipartService;

import io.minio.ObjectWriteResponse;

@RestController
@RequestMapping("/minio/presigned-multipart")
public class MinioMultipartController {
    @Autowired
    private MinioMultipartService multipartService;

    @PostMapping("/initiate")
    public ResponseEntity<Response<MultipartUploadResponse>> initiateUpload(
            @RequestParam String objectName,
            @RequestParam(defaultValue = "application/octet-stream") String contentType) {
        try {
            return Response.ok(multipartService.initiateMultipartUpload(objectName, contentType));
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get presigned URLs for uploading parts
     */
    @PostMapping("/presigned-urls")
    public ResponseEntity<Response<List<PartPresignedUrl>>> getPresignedUrls(
            @RequestParam String objectName,
            @RequestParam String uploadId,
            @RequestParam int totalParts) {
        try {
            return Response.ok(multipartService.generatePresignedUrlsForParts(
                    objectName, uploadId, totalParts));
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get presigned URL for a single part
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<Response<Map<String, Object>>> getPresignedUrl(
            @RequestParam String objectName,
            @RequestParam String uploadId,
            @RequestParam int partNumber) {

        try {
            String presignedUrl = multipartService.generatePresignedUrlForPart(
                    objectName, uploadId, partNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("partNumber", partNumber);
            response.put("presignedUrl", presignedUrl);
            return Response.ok(response);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Complete multipart upload
     */
    @PostMapping("/complete")
    public ResponseEntity<Response<Object>> completeUpload(@RequestBody CompleteUploadRequest request) {

        try {
            ObjectWriteResponse response = multipartService.completeMultipartUpload(request);

            Map<String, String> result = new HashMap<>();
            result.put("etag", response.etag());
            result.put("versionId", response.versionId());
            result.put("objectName", request.getObjectName());
            return Response.ok(result);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Abort multipart upload
     */
    @DeleteMapping("/abort")
    public ResponseEntity<Response<Map<String, String>>> abortUpload(
            @RequestParam String uploadId) {
        try {
            multipartService.abortMultipartUpload(uploadId);

            Map<String, String> result = new HashMap<>();
            result.put("message", "Multipart upload aborted successfully");
            result.put("uploadId", uploadId);
            return Response.ok(result);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get upload information
     */
    @GetMapping("/info")
    public ResponseEntity<Response<MultipartUploadInfo>> getUploadInfo(@RequestParam String uploadId) {

        try {
            MultipartUploadInfo uploadInfo = multipartService.getUploadInfo(uploadId);

            if (uploadInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return Response.ok(uploadInfo);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * List active uploads
     */
    @GetMapping("/active")
    public ResponseEntity<Response<List<MultipartUploadInfo>>> listActiveUploads() {

        try {
            List<MultipartUploadInfo> activeUploads = multipartService.listActiveUploads();
            return Response.ok(activeUploads);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
