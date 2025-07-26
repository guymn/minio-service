package com.pccth.minio.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class MultipartUploadInfo {
    private String bucketName;
    private String objectName;
    private String contentType;
    private Map<String, String> metadata;
    private long createdTime;
}
