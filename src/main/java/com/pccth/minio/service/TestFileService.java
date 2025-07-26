package com.pccth.minio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pccth.minio.entity.TestFileEntity;
import com.pccth.minio.repository.TestFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TestFileService {
    private final TestFileRepository repository;

    public void save(TestFileEntity entity) {
        repository.save(entity);
    }

    public TestFileEntity get(String id) {
        return repository.findById(id).get();
    }
}
