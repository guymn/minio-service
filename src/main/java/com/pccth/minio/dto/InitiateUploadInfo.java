package com.pccth.minio.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class InitiateUploadInfo {
    private String uploadId;
    private String objectName;
    private List<PartPresignedUrl> presignedUrlList;
}
