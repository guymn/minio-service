package com.pccth.minio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PartPresignedUrl {
    private int partNumber;
    private String presignedUrl;
}
