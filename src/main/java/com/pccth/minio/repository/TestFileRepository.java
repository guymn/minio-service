package com.pccth.minio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pccth.minio.entity.TestFileEntity;

public interface TestFileRepository extends MongoRepository<TestFileEntity, String> {

}
