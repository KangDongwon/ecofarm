package com.ecofarm.demo.service;

import com.ecofarm.demo.dto.PendingSellerDTO;
import com.ecofarm.demo.dto.SellerDTO;
import com.ecofarm.demo.entity.Apartment;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.repository.ApartmentRepository;
import com.ecofarm.demo.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SellerService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    public Optional<Seller> findByEmail(String email) { return sellerRepository.findByEmail(email); }

    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    public void deleteById(Long id) {
        sellerRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return sellerRepository.existsByEmail(email);
    }

    public List<PendingSellerDTO> findPendingSellers() { return sellerRepository.findPendingSellers(); }

    public List<PendingSellerDTO> findApprovedSellers() { return sellerRepository.findApprovedSellers(); }

    // 판매자 승인 처리
    public boolean approveSeller(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .map(seller -> {
                    seller.setApprovedDate(LocalDate.now());
                    sellerRepository.save(seller);
                    return true;
                })
                .orElse(false);
    }

    public Optional<PendingSellerDTO> getSellerProfileByEmail(String email) {
        return sellerRepository.findSellerProfileByEmail(email);
    }

    public Seller updateSeller(SellerDTO sellerDTO) {
        Seller seller = sellerRepository.findByEmail(sellerDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없습니다."));

        seller.setSellerName(sellerDTO.getSellerName());
        seller.setPhoneNumber(sellerDTO.getPhoneNumber());

        if (sellerDTO.getPassword() != null && !sellerDTO.getPassword().isEmpty()) {
            seller.setPassword(passwordEncoder.encode(sellerDTO.getPassword()));
        }

        return sellerRepository.save(seller);
    }

    @Transactional
    public void createTestSellers() {
        Random random = new Random();
        for (int i = 1; i <= 150; i++) {
            String name = NAMES[random.nextInt(NAMES.length)];
            String email = "seller" + i + "@test.com";
            String password = passwordEncoder.encode("1111"); // 비밀번호 암호화
            String phoneNumber = String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000));
            LocalDate approvedDate = null;
            if (i > 145) {
                approvedDate = null;
            } else {
                approvedDate = LocalDate.of(2023, 1, 1).plusDays(random.nextInt((int) (LocalDate.now().toEpochDay() - LocalDate.of(2023, 1, 1).toEpochDay() + 1)));
            }

            long apartmentId = random.nextInt(10) + 1; // 1부터 10까지 랜덤하게 아파트 ID 설정
            Optional<Apartment> apartmentOptional = apartmentRepository.findById(apartmentId);
            if (apartmentOptional.isPresent()) {
                Seller seller = new Seller();
                seller.setSellerName(name);
                seller.setEmail(email);
                seller.setPassword(password);
                seller.setPhoneNumber(phoneNumber);
                seller.setApprovedDate(approvedDate);
                seller.setApartment(apartmentOptional.get());

                sellerRepository.save(seller); // 데이터베이스에 저장
            }
        }
    }

    private static final String[] ADDRESSES = {
            "세종시 보람동", "세종시 아름동", "세종시 어진동",
            "세종시 고운동", "세종시 집현동", "대전시 유성구",
            "세종시 세종동", "세종시 나성동", "세종시 한솔동", "세종시 조치원"
    };

    private static final String[] NAMES = {
            "김철수", "이영희", "이을서", "갑을병", "홍길동",
            "김갑동", "박병남", "홍길순", "아무개", "김이박"
    };
}
