package com.pccth.minio.controller;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.pccth.minio.service.BucketService;
import com.pccth.minio.service.MinioService;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private BucketService bucketService;

    @GetMapping("test")
    public String getMethodName() {
        return new String("test");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            minioService.uploadFile("mybucket", file.getOriginalFilename(), file);
            return ResponseEntity.ok("Upload success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String objectName) {
        try {
            InputStream file = minioService.downloadFile("mybucket", objectName);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + objectName + "\"")
                    .body(new InputStreamResource(file));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<String> presigned(
            @RequestParam String bucket,
            @RequestParam String object,
            @RequestParam(defaultValue = "5") int expiryMinutes) {
        try {
            String url = minioService.generatePresignedUrl(bucket, object, 3600);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating URL: " + e.getMessage());
        }
    }

    @PostMapping("/bucket/public")
    public ResponseEntity<String> setPublic() {
        try {
            bucketService.setBucketPublic("mybucket");
            return ResponseEntity.ok("Bucket is now public");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/bucket/private")
    public ResponseEntity<String> setPrivate() {
        try {
            bucketService.setBucketPrivate("mybucket");
            return ResponseEntity.ok("Bucket is now private");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
