package com.ecofarm.demo.repository;

import com.ecofarm.demo.dto.SaleProductDetailDTO;
import com.ecofarm.demo.dto.SaleProductResponseDTO;
import com.ecofarm.demo.entity.SaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {

    @Query("SELECT new com.ecofarm.demo.dto.SaleProductResponseDTO(sp.saleProductID, a.apartmentName, s.sellerID, s.sellerName, p.productName, sp.price, sp.quantity, sp.createdDate, sp.availableUntilDate, sp.approvalDate, sp.approvalStatus, sp.youTubeLink) " +
            "FROM SaleProduct sp " +
            "JOIN sp.seller s " +
            "JOIN sp.product p " +
            "JOIN s.apartment a " + // 추가된 조인
            "WHERE s.email = :email")
    List<SaleProductResponseDTO> findSaleProductsBySellerEmail(@Param("email") String email);


    List<SaleProduct> findByApprovalStatus(String approvalStatus);
    List<SaleProduct> findByApprovalStatusIn(List<String> approvalStatuses);

    @Query("SELECT new com.ecofarm.demo.dto.SaleProductDetailDTO(sp.saleProductID, s.sellerName, p.productName, sp.price, sp.quantity, sp.availableUntilDate, sp.youTubeLink, " +
            "c.consumerName) " +
            "FROM SaleProduct sp " +
            "JOIN sp.seller s " +
            "JOIN sp.product p " +
            "LEFT JOIN sp.reviews r " +
            "LEFT JOIN r.consumer c " +
            "WHERE sp.saleProductID = :saleProductID")
    Optional<SaleProductDetailDTO> findSaleProductDetailById(@Param("saleProductID") Long saleProductID);
}
