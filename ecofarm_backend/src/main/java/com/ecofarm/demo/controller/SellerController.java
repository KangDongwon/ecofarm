package com.ecofarm.demo.controller;


import com.ecofarm.demo.auth.dto.ErrorResponse;
import com.ecofarm.demo.auth.dto.JsonResponse;
import com.ecofarm.demo.dto.ConsumerDTO;
import com.ecofarm.demo.dto.PendingSellerDTO;
import com.ecofarm.demo.dto.SellerDTO;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.entity.Apartment;
import com.ecofarm.demo.service.SellerService;
import com.ecofarm.demo.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ApartmentService apartmentService; // 인스턴스 변수로 선언

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<SellerDTO> getAllSellers() {
        return sellerService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/approved")
    public List<PendingSellerDTO> getApprovedAllSellers() {
        return sellerService.findApprovedSellers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<PendingSellerDTO> getPendingAllSellers() {
        return sellerService.findPendingSellers();
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{id}")
    public ResponseEntity<SellerDTO> getSellerById(@PathVariable Long id) {
        Optional<Seller> seller = sellerService.findById(id);
        return seller.map(s -> ResponseEntity.ok(convertToDTO(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/profile/{email}")
    public ResponseEntity<PendingSellerDTO> getSellerProfile(@PathVariable String email) {
        Optional<PendingSellerDTO> optionalSellerDTO = sellerService.getSellerProfileByEmail(email);
        if (optionalSellerDTO.isPresent()) {
            return ResponseEntity.ok(optionalSellerDTO.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/update")
    public ResponseEntity<Seller> updateSeller(@RequestBody SellerDTO sellerDTO) {
        try {
            // 판매자 업데이트 처리
            Seller updatedSeller = sellerService.updateSeller(sellerDTO);
            return ResponseEntity.ok().body(updatedSeller);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        sellerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{sellerId}")
    public ResponseEntity<?> approveSeller(@PathVariable Long sellerId) {
        boolean isApproved = sellerService.approveSeller(sellerId);
        if (isApproved) {
            return ResponseEntity.ok(new JsonResponse("판매자가 승인되었습니다."));
        } else {
            return ResponseEntity.status(404).body(new ErrorResponse("판매자를 찾을 수 없습니다."));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerConsumer(@RequestBody SellerDTO sellerDTO) {
        if (sellerDTO.getPassword() == null || sellerDTO.getPassword().length() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Password is empty."));
        }
        // 이메일 중복 체크
        if (sellerService.existsByEmail(sellerDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already in use."));
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(sellerDTO.getPassword());
        sellerDTO.setPassword(encodedPassword);
        sellerDTO.setApprovedDate(null);
        // Seller 엔티티 생성 및 저장
        Seller seller = convertToEntity(sellerDTO);
        sellerService.save(seller);

        return ResponseEntity.status(HttpStatus.CREATED).body(new JsonResponse("Seller registered successfully."));
    }

    private SellerDTO convertToDTO(Seller seller) {
        return SellerDTO.builder()
                .sellerID(seller.getSellerID())
                .sellerName(seller.getSellerName())
                .phoneNumber(seller.getPhoneNumber())
                .apartmentID(seller.getApartment() != null ? seller.getApartment().getApartmentID() : null)
                .approvedDate(seller.getApprovedDate())
                .email(seller.getEmail())
                .password(seller.getPassword())
                .build();
    }

    private Seller convertToEntity(SellerDTO dto) {
        Seller seller = new Seller();
        seller.setSellerID(dto.getSellerID());
        seller.setSellerName(dto.getSellerName());
        seller.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getApartmentID() != null) {
            // ApartmentID를 사용하여 Apartment 엔티티를 조회
            Optional<Apartment> apartment = apartmentService.findById(dto.getApartmentID());
            if (apartment.isPresent()) {
                seller.setApartment(apartment.get());
            } else {
                // 적절한 예외 처리 추가
                throw new RuntimeException("Apartment not found");
            }
        }

        seller.setApprovedDate(dto.getApprovedDate());
        seller.setEmail(dto.getEmail());
        seller.setPassword(dto.getPassword());
        return seller;
    }

}
