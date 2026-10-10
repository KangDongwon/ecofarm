package com.ecofarm.demo.repository;

import com.ecofarm.demo.dto.PendingSellerDTO;
import com.ecofarm.demo.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    boolean existsByEmail(String email);

    // 이메일로 소비자 조회
    Optional<Seller> findByEmail(String email);

    Optional<Seller> findByEmailAndApprovedDateNotNull(String email);

    @Query("SELECT new com.ecofarm.demo.dto.PendingSellerDTO(s.sellerID, s.sellerName, s.phoneNumber, s.email, a.apartmentName, s.approvedDate) FROM Seller s JOIN s.apartment a WHERE s.approvedDate IS NULL")
    List<PendingSellerDTO> findPendingSellers();

    @Query("SELECT new com.ecofarm.demo.dto.PendingSellerDTO(s.sellerID, s.sellerName, s.phoneNumber, s.email, a.apartmentName, s.approvedDate) FROM Seller s JOIN s.apartment a WHERE s.approvedDate IS NOT NULL")
    List<PendingSellerDTO> findApprovedSellers();

    @Query("SELECT new com.ecofarm.demo.dto.PendingSellerDTO(s.sellerID, s.sellerName, s.phoneNumber, s.email, a.apartmentName, s.approvedDate) FROM Seller s JOIN s.apartment a WHERE s.email = :email")
    Optional<PendingSellerDTO> findSellerProfileByEmail(@Param("email") String email);
}
