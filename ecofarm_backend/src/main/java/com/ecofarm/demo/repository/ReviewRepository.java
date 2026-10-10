package com.ecofarm.demo.repository;

import com.ecofarm.demo.dto.ReviewResponseDTO;
import com.ecofarm.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT new com.ecofarm.demo.dto.ReviewResponseDTO(" +
            "r.reviewID, " +
            "p.productName, " + // product 테이블에서 productName
            "co.orderDate, " +
            "s.sellerName, " + // seller 테이블에서 sellerName
            "r.consumer.consumerID, " +
            "r.consumer.consumerName, " +
            "sp.saleProductID, " +
            "r.rating, " +
            "r.comment, " +
            "r.reviewDate) " +
            "FROM Review r " +
            "JOIN r.saleProduct sp " + // saleProduct와 조인
            "JOIN sp.product p " + // SaleProduct와 Product 테이블 조인
            "JOIN sp.seller s " + // SaleProduct와 Seller 테이블 조인
            "JOIN r.customerOrder co " +
            "WHERE r.consumer.email = :email")
    List<ReviewResponseDTO> findByConsumerEmail(@Param("email") String email);

    @Query("SELECT new com.ecofarm.demo.dto.ReviewResponseDTO(" +
            "r.reviewID, " +
            "p.productName, " + // product 테이블에서 productName
            "co.orderDate, " +
            "s.sellerName, " + // seller 테이블에서 sellerName
            "r.consumer.consumerID, " +
            "r.consumer.consumerName, " +
            "sp.saleProductID, " +
            "r.rating, " +
            "r.comment, " +
            "r.reviewDate) " +
            "FROM Review r " +
            "JOIN r.saleProduct sp " + // saleProduct와 조인
            "JOIN sp.product p " + // SaleProduct와 Product 테이블 조인
            "JOIN sp.seller s " + // SaleProduct와 Seller 테이블 조인
            "JOIN r.customerOrder co " +
            "WHERE s.sellerID = :id")
    List<ReviewResponseDTO> findBySeller(@Param("id") Long id);

}
