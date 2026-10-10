package com.ecofarm.demo.service;

import com.ecofarm.demo.entity.Admin;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminRepository adminRepository;

    // 모든 관리자 조회
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    // ID로 관리자 조회
    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    // 관리자 저장
    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }

    // ID로 관리자 삭제
    public void deleteById(Long id) {
        adminRepository.deleteById(id);
    }

    // 이메일 중복 확인
    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    // 생성자
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Transactional
    public void createTestAdmin() {
        Admin admin = new Admin();
        admin.setAdminName("관리자");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("1111"));

        adminRepository.save(admin);
    }
}
