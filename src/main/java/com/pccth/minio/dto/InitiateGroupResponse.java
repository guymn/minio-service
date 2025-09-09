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
public class InitiateGroupResponse {
    private String groupId;
    private List<InitiateUploadInfo> initiateUploadList;
}
