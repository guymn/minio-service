package com.pccth.minio.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TestFile")
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestFileEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String fileName;
    
    private Long size;

    private String bucket;
}
