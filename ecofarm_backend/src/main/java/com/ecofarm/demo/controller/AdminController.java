package com.ecofarm.demo.controller;

import com.ecofarm.demo.dto.AdminDTO;
import com.ecofarm.demo.dto.ConsumerDTO;
import com.ecofarm.demo.entity.Admin;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<AdminDTO> getAllAdmins() {
        return adminService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AdminDTO> getAdminById(@PathVariable Long id) {
        return adminService.findById(id)
                .map(admin -> ResponseEntity.ok(convertToDTO(admin)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminDTO adminDTO) {
        // 이메일 중복 체크
        if (adminService.existsByEmail(adminDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(adminDTO.getPassword());
        adminDTO.setPassword(encodedPassword);

        // Consumer 엔티티 생성 및 저장
        Admin admin = convertToEntity(adminDTO);
        adminService.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body("Admin registered successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDTO> updateAdmin(@PathVariable Long id, @RequestBody AdminDTO adminDTO) {
        return adminService.findById(id)
                .map(admin -> {
                    Admin updatedAdmin = convertToEntity(adminDTO);
                    updatedAdmin.setAdminID(id);
                    Admin savedAdmin = adminService.save(updatedAdmin);
                    return ResponseEntity.ok(convertToDTO(savedAdmin));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        if (adminService.findById(id).isPresent()) {
            adminService.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private AdminDTO convertToDTO(Admin admin) {
        return AdminDTO.builder()
                .adminID(admin.getAdminID())
                .adminName(admin.getAdminName())
                .email(admin.getEmail())
                .password(admin.getPassword())
                .build();
    }

    private Admin convertToEntity(AdminDTO adminDTO) {
        Admin admin = new Admin();
        admin.setAdminID(adminDTO.getAdminID());
        admin.setAdminName(adminDTO.getAdminName());
        admin.setEmail(adminDTO.getEmail());
        admin.setPassword(adminDTO.getPassword());
        return admin;
    }
}
