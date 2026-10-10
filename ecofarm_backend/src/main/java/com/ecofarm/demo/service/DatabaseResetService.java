package com.ecofarm.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseResetService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void resetAutoIncrement(String tableName) {
        // 모든 데이터 삭제
        entityManager.createNativeQuery("DELETE FROM " + tableName).executeUpdate();

        // AUTO_INCREMENT 리셋
        entityManager.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
    }
}
