package com.pccth.minio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class FileInfoDto {
    private String fileName;
    private String bucketName;
    private String contentType;
    private Integer countPart;
}
