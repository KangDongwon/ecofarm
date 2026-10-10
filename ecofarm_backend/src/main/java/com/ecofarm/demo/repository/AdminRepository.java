package com.ecofarm.demo.repository;

import com.ecofarm.demo.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByEmail(String email);

    // 이메일로 소비자 조회
    Optional<Admin> findByEmail(String email);
}