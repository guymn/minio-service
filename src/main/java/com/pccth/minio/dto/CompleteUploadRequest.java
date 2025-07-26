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
public class CompleteUploadRequest {
    String objectName;
    String uploadId;
    List<PartInfo> parts;
}
