package com.pccth.minio.dto;

import java.util.Date;

public record FileVersionInfo(
        String key,
        long size,
        String versionId,
        boolean isLatest,
        Date lastModified) {
}