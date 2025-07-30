package com.pccth.minio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pccth.minio.dto.FileVersionInfo;
import com.pccth.minio.dto.Response;
import com.pccth.minio.service.MinioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/minio")
public class MinioController {
    @Autowired
    private MinioService multipartService;

    @GetMapping("presignd-url/download")
    public ResponseEntity<Response<String>> getMethodName(@RequestParam String objectName) {
        try {
            return Response.ok(multipartService.generatePresignedUrlDownload(objectName));
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response<List<FileVersionInfo>>> getList(@RequestParam String objectKey) {
        try {
            List<FileVersionInfo> uploadInfo = multipartService.listObjectVersions(objectKey);

            return Response.ok(uploadInfo);
        } catch (Exception e) {
            return Response.withStatusAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
